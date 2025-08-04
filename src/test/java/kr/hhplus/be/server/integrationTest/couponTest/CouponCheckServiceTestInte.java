package kr.hhplus.be.server.integrationTest.couponTest;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class CouponCheckServiceTestInte {


    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @Autowired
    private CouponCheckService service;

    private Long userId;
    private Long couponId;

    @BeforeEach
    void setUp() {


        userId = 100L;
        couponId = saveCoupon().getCouponId();  // DB에 실제 쿠폰 저장
    }

    private CouponJPA saveCoupon() {
        return couponRepository.save(new CouponJPA(
                null,
                "10% 할인",
                10,
                100,
                50,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
    }

    @Test
    @DisplayName("정상적인 쿠폰은 유효성 검사를 통과한다")
     void checkCoupon_valid() {
        // given
        couponUserRepository.save(new CouponUserJPA(
                null, userId, couponId, false,
                null, LocalDateTime.now()
        ));

//        System.out.println("couponId: " + couponId);
//        System.out.println("userId: " + userId);

        // when
        service.checkCoupon(userId, couponId);
        // then: 예외 없이 통과하면 성공
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰은 예외가 발생한다")
    void checkCoupon_notFound() {
        // given: couponId를 999로 일부러 틀림
        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 쿠폰입니다.");
    }

    @Test
    @DisplayName("쿠폰 사용기간이 아닐 경우 예외가 발생한다")
    void checkCoupon_invalidPeriod() {
        // given
        CouponJPA expiredCoupon = couponRepository.save(new CouponJPA(
                null, "만료 쿠폰", 10, 100, 10,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        ));

        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, expiredCoupon.getCouponId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("쿠폰 사용기간이 아닙니다.");
    }

    @Test
    @DisplayName("사용자가 쿠폰을 보유하고 있지 않으면 예외가 발생한다")
    void checkCoupon_userNotOwnsCoupon() {
        // given: 쿠폰은 있으나 사용자에게 발급하지 않음
        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자는 이 쿠폰을 보유하고 있지 않습니다.");
    }

    @Test
    @DisplayName("이미 사용한 쿠폰이면 예외가 발생한다")
    void checkCoupon_alreadyUsed() {
        // given
        couponUserRepository.save(new CouponUserJPA(
                null, userId, couponId, true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(2)
        ));

        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, couponId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 사용된 쿠폰입니다.");
    }


}
