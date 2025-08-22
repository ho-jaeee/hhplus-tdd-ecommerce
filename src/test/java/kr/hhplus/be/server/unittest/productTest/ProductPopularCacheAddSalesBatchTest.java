package kr.hhplus.be.server.unittest.productTest;

import kr.hhplus.be.server.product.domain.service.ProductPopularCacheServiceImpl;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPopularCacheAddSalesBatchTest {
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ZSetOperations<String, String> zsetOps;
    @Mock private HashOperations<String, Object, Object> hashOps;

    private ProductPopularCacheServiceImpl sut;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DAILY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);

        // 테스트 대상 구현체를 주입하세요.
        sut = new ProductPopularCacheServiceImpl(redis);
    }

    @Test
    void addSalesBatch_firstSeen_increments_scores_puts_meta_and_sets_ttl() {
        // given
        String eventId = "evt-123";
        Instant createdAt = Instant.parse("2025-08-21T02:30:00Z"); // KST: 2025-08-21
        List<ProductPopularDto> items = List.of(
                new ProductPopularDto(1001L, "상품A", 2),
                new ProductPopularDto(2002L, "상품B", 1)
        );

        // 최초 이벤트(처리해야 함)
        when(valueOps.setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any()))
                .thenReturn(Boolean.TRUE);

        // 이 테스트 경로에서 실제 호출되는 것만 스텁
        when(redis.opsForZSet()).thenReturn(zsetOps);
        when(redis.opsForHash()).thenReturn(hashOps);

        // 파이프라인 콜백을 "가짜로" 실행: 콜백 내부에서 redis(동일 mock)를 사용하므로 그대로 넘겨줌
        when(redis.executePipelined(any(RedisCallback.class))).thenAnswer(inv -> {
            RedisCallback<?> cb = inv.getArgument(0);
            cb.doInRedis(mock(RedisConnection.class));
            return List.of();
        });

        String dailyKey = "rank:product:sales:daily:" +
                DAILY_FMT.format(LocalDateTime.ofInstant(createdAt, KST));

        // when
        sut.addSalesBatch(items, createdAt, eventId);

        // then: 점수 누적
        verify(zsetOps).incrementScore(dailyKey, "1001", 2.0);
        verify(zsetOps).incrementScore(dailyKey, "2002", 1.0);

        // 메타 저장(putIfAbsent)
        verify(hashOps).putIfAbsent("product:meta", "1001", "상품A");
        verify(hashOps).putIfAbsent("product:meta", "2002", "상품B");

        // TTL 10일 설정
        verify(redis).expire(eq(dailyKey), eq(Duration.ofDays(10)));

        // 멱등성 키 설정 호출 확인
        verify(valueOps).setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any());

        verifyNoMoreInteractions(zsetOps, hashOps);
    }

    @Test
    void addSalesBatch_duplicateEvent_doesNothing() {
        // given
        String eventId = "evt-dup";
        Instant createdAt = Instant.parse("2025-08-21T00:00:00Z");
        List<ProductPopularDto> items = List.of(
                new ProductPopularDto(3003L, "상품C", 5)
        );

        // 이미 처리된 이벤트 → 조기 리턴
        when(valueOps.setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any()))
                .thenReturn(Boolean.FALSE);

        // when
        sut.addSalesBatch(items, createdAt, eventId);

        // then: 어떤 변경도 없어야 함 (ZSET/HASH/expire 호출 없음)
        verify(valueOps).setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any());
        verifyNoInteractions(zsetOps);
        verifyNoInteractions(hashOps);
        verify(redis, never()).expire(anyString(), any());
    }
}
