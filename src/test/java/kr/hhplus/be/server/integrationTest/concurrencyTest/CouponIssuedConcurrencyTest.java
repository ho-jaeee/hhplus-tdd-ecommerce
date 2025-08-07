package kr.hhplus.be.server.integrationTest.concurrencyTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
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
@Import({TestcontainersConfiguration.class})
public class CouponIssuedConcurrencyTest {

    @Autowired
    private CouponIssuedService couponIssuedService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    private long couponId;

    @BeforeEach
    void setUp() {
        // 기본 테스트용 쿠폰 저장 (발급 한도: 10개)
        CouponJPA coupon = couponRepository.save(new CouponJPA(
                null,
                "10% 할인쿠폰",
                10,         // 할인율
                10,         // 총 발급 수량
                0,          // 현재 발급 수량
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        couponId = coupon.getCouponId();
    }

    @Test
    @DisplayName("쿠폰 동시발급 테스트 10개의 쿠폰 20개의 요청")
    void couponIssuedConcurrencyTest() throws InterruptedException {


        // given
        int threadCount = 20; // 동시 요청 수
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            final long userId = i + 1L;
            executorService.submit(() -> {
                try {
                    couponIssuedService.issueCouponToUser(couponId, userId);
                } catch (Exception e) {
                    // 예외 발생 무시 (중복 발급, 수량 초과 등)
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 요청 완료까지 대기

        // then
        long actualIssuedCount = couponUserRepository.count();
        assertThat(actualIssuedCount).isEqualTo(10L); // 10개까지만 발급돼야 함

    }
}
