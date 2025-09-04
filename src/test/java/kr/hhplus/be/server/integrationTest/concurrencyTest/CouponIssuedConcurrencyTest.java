package kr.hhplus.be.server.integrationTest.concurrencyTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.config.RedissonTestConfig;
import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRepository;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.couponRedis.usecase.CouponIssuedUseCase;
import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestcontainersConfiguration.class, RedissonTestConfig.class})
public class CouponIssuedConcurrencyTest {


    @Autowired
    CouponIssuedUseCase couponIssuedUseCase;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    private long couponId;
    private int limit = 10;

    @BeforeEach
    void setUp() {
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
        couponId = coupon.getCouponId();
    }

    @AfterEach
    void tearDown() {
        couponUserRepository.deleteAll();
        // 쿠폰은 깨끗이 비우고 싶으면 별도 메서드 준비 (테스트 컨테이너면 매 테스트 격리되기도 함)
        // e.g., couponRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 20요청 → 발급은 한도(10)까지만 성공")
    void couponIssuedConcurrencyTest() throws InterruptedException {
        // given
        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1L;
            pool.submit(() -> {
                try {
                    // reqId는 외부에 공개하지 않음 → null 전달(UseCase가 내부 생성)
                    couponIssuedUseCase.issueCoupon(new CouponIssueCommand(couponId, userId, null));
                } catch (Exception ignore) {
                    // 동시성/컷오프/중복 예외는 테스트 목적상 무시
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        // then: coupon_user 발급건수는 한도와 동일해야 함
        long actualIssuedCount = couponUserRepository.count();
        assertThat(actualIssuedCount).isEqualTo(limit);

        // 그리고 쿠폰 집계 필드(issuedQuantity)도 한도와 동일하게 증가했는지 확인
        CouponJPA refreshed = couponRepository.findById(couponId).orElseThrow();
        assertThat(refreshed.getIssuedQuantity()).isEqualTo(limit);
    }

    @Test
    @DisplayName("같은 유저가 여러 번 요청해도 중복 발급되지 않는다")
    void sameUserMultipleRequests_noDuplicate() throws InterruptedException {
        // given
        int threadCount = 5; // 같은 유저가 5번 연타
        long userId = 999L;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    couponIssuedUseCase.issueCoupon(new CouponIssueCommand(couponId, userId, null));
                } catch (Exception ignore) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();

        // then: 해당 유저의 발급 레코드는 1건이어야 함
        long countForUser = couponUserRepository.findAll().stream()
                .filter(e -> e.getUserId().equals(userId) && e.getCouponId().equals(couponId))
                .count();
        assertThat(countForUser).isEqualTo(1L);
    }
}
