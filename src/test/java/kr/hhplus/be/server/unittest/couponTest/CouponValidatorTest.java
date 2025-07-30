package kr.hhplus.be.server.unittest.couponTest;

import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import kr.hhplus.be.server.coupon.policy.CouponValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class CouponValidatorTest {

    private CouponValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CouponValidator();
    }

    @Test
    @DisplayName("정상적인 쿠폰은 발급 검증을 통과한다")
    void validateSuccess() {
        // given
        Coupon coupon = new Coupon(
                1L, "10% 할인쿠폰", 10, 100, 50,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );

        // when / then
        assertThatCode(() -> validator.validateIssue(coupon, Optional.empty()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("쿠폰 유효기간이 아니면 예외가 발생한다")
    void invalidPeriodThrowsException() {
        // given
        Coupon coupon = new Coupon(
                2L, "기간 종료", 10, 100, 50,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );

        // when / then
        assertThatThrownBy(() -> validator.validateIssue(coupon, Optional.empty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("유효기간");
    }

    @Test
    @DisplayName("발급 수량을 초과한 쿠폰이면 예외가 발생한다")
    void overQuantityThrowsException() {
        // given
        Coupon coupon = new Coupon(
                3L, "수량 초과", 10, 100, 100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );

        // when / then
        assertThatThrownBy(() -> validator.validateIssue(coupon, Optional.empty()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("발급 수량");
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰이면 예외가 발생한다")
    void alreadyIssuedThrowsException() {
        // given
        Coupon coupon = new Coupon(
                4L, "중복 발급", 10, 100, 50,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );

        CouponUser issued = new CouponUser(1L, 100L, false,null, LocalDateTime.now());

        // when / then
        assertThatThrownBy(() -> validator.validateIssue(coupon, Optional.of(issued)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 발급");
    }


}
