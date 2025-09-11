package kr.hhplus.be.server.integrationTest.couponTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.config.RedissonTestConfig;
import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;
import kr.hhplus.be.server.couponRedis.domain.model.CouponUserJPA;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRepository;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponUserRepository;

import kr.hhplus.be.server.couponRedis.usecase.CouponIssuedUseCase;
import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueCommand;
import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import({TestcontainersConfiguration.class, RedissonTestConfig.class})
public class CouponIssuedUseCastTestIntegration {


    @Autowired
    private CouponIssuedUseCase useCase;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @Autowired
    private RedissonClient redissonClient;

    private static CouponIssueCommand cmd(Long couponId, Long userId, String reqId) {
        return new CouponIssueCommand(couponId, userId, reqId);
    }

    private long id;
    private int limit = 10;

    /** 테스트마다 깨끗한 상태 보장 및 쿠폰 시드 */
    @BeforeEach
    void setUp() {
        couponUserRepository.deleteAll();

        RKeys keys = redissonClient.getKeys();
        keys.deleteByPattern("coupon:*");

        // 테스트용 쿠폰 저장 (총 발급 수량 limit, 현재 0)
        CouponJPA coupon = couponRepository.save(new CouponJPA(
                null,
                "10% 할인쿠폰",
                10,                     // 할인액/율 (도메인에 맞게)
                limit,                  // 총 발급 수량
                0,                      // 현재 발급 수량
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        id = coupon.getCouponId();
    }



    @Test
    @Order(1)
    @DisplayName("발급 성공: 유효한 입력이면 실제 DB에 발급 레코드가 적재된다")
    void issue_success_persists() {
        // given
        Long couponId = id;
        Long userId = 10001L;
        String reqId = "req-success-1";

        // when
        CouponIssueResult result = useCase.issueCoupon(cmd(couponId, userId, reqId));

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.couponId()).isEqualTo(couponId);

        Optional<CouponUserJPA> saved = couponUserRepository.findByReqId(reqId);
        assertThat(saved).isPresent();
        // 엔티티 타입/필드에 맞게 검증
        assertThat(saved.get().getReqId()).isEqualTo(reqId);
    }

    @Test
    @Order(2)
    @DisplayName("멱등성: 동일 reqId로 두 번 요청해도 발급은 1건만")
    void idempotency_same_reqId_single_record() {
        // given
        Long couponId = id;
        Long userId = 10002L;
        String reqId = "req-idem-1";

        // when
        CouponIssueResult r1 = useCase.issueCoupon(cmd(couponId, userId, reqId));
        CouponIssueResult r2 = useCase.issueCoupon(cmd(couponId, userId, reqId));

        // then
        assertThat(r1.isSuccess()).isTrue();
        assertThat(r2.isSuccess()).isFalse();

        Long count = couponUserRepository.countByReqId(reqId);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @Order(3)
    @DisplayName("입력 검증: 잘못된 입력(null/<=0)일 때 FAIL이며 DB 반영 없음")
    void validation_fail_no_persist() {
        CouponIssueResult r1 = useCase.issueCoupon(cmd(null, 1L, "req-bad-1"));
        CouponIssueResult r2 = useCase.issueCoupon(cmd(1L, null, "req-bad-2"));
        CouponIssueResult r3 = useCase.issueCoupon(cmd(-1L, 2L, "req-bad-3"));
        CouponIssueResult r4 = useCase.issueCoupon(cmd(2L, -1L, "req-bad-4"));

        assertThat(r1.isSuccess()).isFalse();
        assertThat(r2.isSuccess()).isFalse();
        assertThat(r3.isSuccess()).isFalse();
        assertThat(r4.isSuccess()).isFalse();

        long sum = safeCount("req-bad-1") + safeCount("req-bad-2") + safeCount("req-bad-3") + safeCount("req-bad-4");
        assertThat(sum).isZero();
    }


    private long safeCount(String reqId) {
        Long c = couponUserRepository.countByReqId(reqId);
        return c == null ? 0L : c;
    }



}
