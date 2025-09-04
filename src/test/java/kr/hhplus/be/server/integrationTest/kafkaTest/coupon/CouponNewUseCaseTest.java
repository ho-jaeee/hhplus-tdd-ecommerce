package kr.hhplus.be.server.integrationTest.kafkaTest.coupon;


import kr.hhplus.be.server.TestcontainersConfiguration;


import kr.hhplus.be.server.couponRedisKafka.domain.model.CouponNewJPA;
import kr.hhplus.be.server.couponRedisKafka.domain.model.CouponUserNewJPA;
import kr.hhplus.be.server.couponRedisKafka.domain.repository.CouponNewJpaRepository;
import kr.hhplus.be.server.couponRedisKafka.domain.repository.CouponUserNewJpaRepository;
import kr.hhplus.be.server.couponRedisKafka.usecase.CouponIssuedUseCaseNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueCommandNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueResultNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.IssueResultCode;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;




@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class CouponNewUseCaseTest {
    @Autowired private CouponIssuedUseCaseNew useCase;

    // JPA 리포지토리 사용
    @Autowired private CouponNewJpaRepository couponNewRepository;
    @Autowired private CouponUserNewJpaRepository couponUserNewRepository;

    // Redis 키 초기화용
    @Autowired private RedissonClient redissonClient;

    private long couponId;  // 매 테스트마다 시드한 쿠폰 ID
    private final int limit = 10;

    private static CouponIssueCommandNew cmd(Long couponId, Long userId, String reqId, Instant at) {
        return new CouponIssueCommandNew(couponId, userId, reqId, at);
    }

    /** 매 테스트 전: JPA로 DB 정리/시드 + Redis 키 초기화 */
    @BeforeEach
    void setUp() {
        // 1) DB 정리 (JPA)
        couponNewRepository.deleteAll();
        couponUserNewRepository.deleteAll();

        // 2) Redis 키 정리 (게이트키퍼 프리픽스에 맞게)
        RKeys keys = redissonClient.getKeys();
        keys.deleteByPattern("seq:*");
        keys.deleteByPattern("pending:*");
        keys.deleteByPattern("rid:*");
        keys.deleteByPattern("coupon:*");
        keys.deleteByPattern("gatekeeper:*");

        // 3) 쿠폰 시드 (JPA)
        CouponNewJPA saved = couponNewRepository.save(
                CouponNewJPA.builder()
                        .id(null)
                        .total(10L)
                        .issued(0L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(null)
                        .build()
        );


        couponId = saved.getId();
    }

    @Test
    @DisplayName("발급 성공: 유효 입력 → 유스케이스 SUCCESS/OK 응답, 컨슈머 완료 후 DB 1건 적재(JPA로 확인)")
    void issue_success_persists() {
        // given
        Long userId = 10001L;
        String reqId = "req-success-1";
        Instant at = Instant.parse("2025-09-04T11:00:00Z");

        // when — 동기 응답 확인
        CouponIssueResultNew result = useCase.issue(cmd(couponId, userId, reqId, at));

        // then — 응답만 우선 확인
        assertThat(result.code()).isIn(IssueResultCode.SUCCESS, IssueResultCode.SUCCESS);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.couponId()).isEqualTo(couponId);
        assertThat(result.rank()).isGreaterThanOrEqualTo(1L);

        // then — 비동기 컨슈머가 DB에 적재할 때까지 기다렸다가 JPA로 검증
        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> {
            Optional<CouponUserNewJPA> saved = couponUserNewRepository.findByIdemKey(reqId);
            assertThat(saved).isPresent();
            assertThat(saved.get().getCouponId()).isEqualTo(couponId);
            assertThat(saved.get().getUserId()).isEqualTo(userId);
        });
    }

    @Test
    @DisplayName("멱등성: 동일 requestId 두 번 → 최종 DB 적재는 1건만(JPA count)")
    void idempotency_same_reqId_single_record() {
        Long userId = 10002L;
        String reqId = "req-idem-1";
        Instant at = Instant.parse("2025-09-04T11:05:00Z");

        var r1 = useCase.issue(cmd(couponId, userId, reqId, at));
        var r2 = useCase.issue(cmd(couponId, userId, reqId, at));

        assertThat(r1.code()).isIn(IssueResultCode.SUCCESS, IssueResultCode.SUCCESS);
        assertThat(r2.code()).isIn(IssueResultCode.SUCCESS, IssueResultCode.SUCCESS);

        Awaitility.await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(couponUserNewRepository.countByIdemKey(reqId)).isEqualTo(1L)
        );
    }

    @Test
    @DisplayName("입력 검증: 잘못된 입력(null/음수) → INVALID_ARGUMENT, DB 반영 없음")
    void validation_fail_no_persist() {

        String[] badKeys = {"req-bad-1", "req-bad-2", "req-bad-3", "req-bad-4"};

        var r1 = useCase.issue(cmd(null, 1L, "req-bad-1", Instant.now()));
        var r2 = useCase.issue(cmd(couponId, null, "req-bad-2", Instant.now()));
        var r3 = useCase.issue(cmd(-1L, 2L, "req-bad-3", Instant.now()));
        var r4 = useCase.issue(cmd(couponId, -1L, "req-bad-4", Instant.now()));

        assertThat(r1.code()).isEqualTo(IssueResultCode.INVALID_ARGUMENT);
        assertThat(r2.code()).isEqualTo(IssueResultCode.INVALID_ARGUMENT);
        assertThat(r3.code()).isEqualTo(IssueResultCode.INVALID_ARGUMENT);
        assertThat(r4.code()).isEqualTo(IssueResultCode.INVALID_ARGUMENT);

        // 잠깐 대기 후 최종 DB 확인 (없어야 함)
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    long total = Arrays.stream(badKeys)
                            .mapToLong(k -> couponUserNewRepository.countByIdemKey(k))
                            .sum();
                    assertThat(total).isZero();
                });
    }

    @Test
    @DisplayName("수량 한도: limit 초과 요청해도 최종 DB 발급은 limit까지만")
    void stock_guard_limit() {
        int extra = 5;
        for (int i = 0; i < limit + extra; i++) {
            Long uid = 20000L + i;
            String reqId = "req-limit-" + i;
            useCase.issue(cmd(couponId, uid, reqId, Instant.now()));
        }
        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            // 전체 발급 건수(JPA)
            Long c = couponUserNewRepository.count();
            assertThat(c).isEqualTo((long) limit);
        });
    }

}
