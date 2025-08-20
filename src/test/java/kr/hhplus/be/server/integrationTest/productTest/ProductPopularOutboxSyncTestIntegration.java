package kr.hhplus.be.server.integrationTest.productTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.config.RedissonTestConfig;
import kr.hhplus.be.server.product.domain.model.OutboxStatus;
import kr.hhplus.be.server.product.domain.model.ProductPopularOutboxJPA;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.ProductPopularOutboxSyncService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import kr.hhplus.be.server.product.infrastructure.SpringDataProductPopularOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import({TestcontainersConfiguration.class, RedissonTestConfig.class})
public class ProductPopularOutboxSyncTestIntegration {

    @Autowired
    ProductPopularOutboxSyncService syncService;                 // 실제 서비스

    @Autowired
    SpringDataProductPopularOutboxRepository outboxJpa;                 // 실제 JPA

    @Autowired jakarta.persistence.EntityManager em;

    @MockBean
    ProductPopularCacheService cacheService;                      // 캐시만 목


    @BeforeEach
    void clean() {
        // 순서 중요: DB → 영속성컨텍스트
        outboxJpa.deleteAll(); // 테이블 비우기
        em.clear();            // 1차 캐시 비우기
    }


    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Test
    @DisplayName("Outbox PENDING 묶음을 캐시에 반영하고 DONE으로 전환한다")
    void sync_success_marks_done_and_calls_cache() {
        // given: Outbox에 PENDING 2건 적재
        Instant eventTimeUtc = LocalDateTime.of(2025, 8, 21, 10, 30).atZone(KST).toInstant();
        var e1 = ProductPopularOutboxJPA.builder()
                .productId(1001L).scoreDelta(2).eventTime(eventTimeUtc)
                .status(OutboxStatus.PENDING)
                .retryCount(0).build();
        var e2 = ProductPopularOutboxJPA.builder()
                .productId(2002L).scoreDelta(5).eventTime(eventTimeUtc)
                .status(OutboxStatus.PENDING)
                .retryCount(0).build();
        outboxJpa.saveAll(List.of(e1, e2));

        // when: 동기화 1회 수행
        int processed = syncService.syncBatch(100);

        // then: 캐시 서비스가 2번 호출되었는지 검증
        ArgumentCaptor<List<ProductPopularDto>> dtoCaptor = ArgumentCaptor.forClass(List.class);
        verify(cacheService, times(2))
                .addSalesBatch(dtoCaptor.capture(), any(Instant.class), startsWith("obx-"));

        // 첫 호출 파라미터 간단 점검 (productId/scoreDelta)
        var firstBatch = dtoCaptor.getAllValues().get(0);
        assertThat(firstBatch).hasSize(1);
        assertThat(firstBatch.get(0).productId()).isIn(1001L, 2002L);
        assertThat(firstBatch.get(0).score()).isIn(2L, 5L);

        // 처리 건수
        assertThat(processed).isEqualTo(2);

        // 그리고 상태가 DONE 으로 바뀌었는지 확인
        var all = outboxJpa.findAll();
        assertThat(all).hasSize(2);
        assertThat(all).allMatch(x -> x.getStatus() == OutboxStatus.DONE);
    }

    @Test
    @DisplayName("캐시 반영 실패 시 항목은 재시도 스케줄이 설정된다")
    void sync_failure_reschedules_backoff() {
        // given: 1건 적재
        Instant eventTimeUtc = LocalDateTime.of(2025, 8, 21, 10, 30).atZone(KST).toInstant();
        var e = ProductPopularOutboxJPA.builder()
                .productId(3003L).scoreDelta(3).eventTime(eventTimeUtc)
                .status(OutboxStatus.PENDING).retryCount(0).build();
        e = outboxJpa.save(e);

        // 캐시 서비스가 예외를 던지게 스텁
        doThrow(new RuntimeException("redis down"))
                .when(cacheService).addSalesBatch(anyList(), any(), anyString());

        // when
        int processed = syncService.syncBatch(100);

        // then: 성공=0, 해당 레코드는 PENDING으로 되돌고 retryCount 증가 & nextRetryAt 설정
        assertThat(processed).isEqualTo(0);

        var after = outboxJpa.findById(e.getId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(after.getRetryCount()).isEqualTo(1);
        assertThat(after.getNextRetryAt()).isNotNull();
    }
}
