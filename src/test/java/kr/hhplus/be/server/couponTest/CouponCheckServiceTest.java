package kr.hhplus.be.server.couponTest;



import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.coupon.domain.service.Coupon;
import kr.hhplus.be.server.coupon.domain.service.CouponUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CouponCheckServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserRepository couponUserRepository;

    @InjectMocks
    private CouponCheckService service;

    private Long userId;
    private Long couponId;

    @BeforeEach
    void setUp() {
        userId = 100L;
        couponId = 1L;
    }


    @Test
    @DisplayName("정상적인 쿠폰은 유효성 검사를 통과한다")
    void checkCoupon() {

        // given
         Coupon coupon = new Coupon( // ERD 구조에 맞춰 생성
                 couponId, "10% 할인", 10, 100, 50,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );

        CouponUser couponUser = new CouponUser(
                 userId, couponId, false, null, LocalDateTime.now()
        );

        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.of(coupon));
        given(couponUserRepository.findByUserIdAndCouponId(userId, couponId)).willReturn(Optional.of(couponUser));


        // When
        service.checkCoupon(userId, couponId);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰은 예외가 발생한다")
    void checkCoupon_notFound() {

        // given
        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 쿠폰입니다.");
    }

    @Test
    @DisplayName("쿠폰 사용기간이 아닐 경우 예외가 발생한다")
    void checkCoupon_invalidPeriod() {
        // given
        Coupon expiredCoupon = new Coupon(
                couponId, "만료 쿠폰", 10, 100, 10,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.of(expiredCoupon));

        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, couponId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("쿠폰 사용기간이 아닙니다.");
    }

    @Test
    @DisplayName("사용자가 쿠폰을 보유하고 있지 않으면 예외가 발생한다")
    void checkCoupon_userNotOwnsCoupon() {
        // given
        Coupon validCoupon = new Coupon(
                couponId, "정상 쿠폰", 10, 100, 10,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.of(validCoupon));
        given(couponUserRepository.findByUserIdAndCouponId(userId, couponId)).willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, couponId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자는 이 쿠폰을 보유하고 있지 않습니다.");
    }

    @Test
    @DisplayName("이미 사용한 쿠폰이면 예외가 발생한다")
    void checkCoupon_alreadyUsed() {
        // given
        Coupon validCoupon = new Coupon(
                couponId, "사용 쿠폰", 10, 100, 10,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );
        CouponUser usedCoupon = new CouponUser(
                userId, couponId, true,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(2)
        );
        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.of(validCoupon));
        given(couponUserRepository.findByUserIdAndCouponId(userId, couponId)).willReturn(Optional.of(usedCoupon));

        // expect
        assertThatThrownBy(() -> service.checkCoupon(userId, couponId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 사용된 쿠폰입니다.");
    }

}
