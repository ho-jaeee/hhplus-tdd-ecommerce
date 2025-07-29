package kr.hhplus.be.server.couponTest;


import kr.hhplus.be.server.coupon.controller.CouponController;
import kr.hhplus.be.server.coupon.controller.dto.CouponRequest;
import kr.hhplus.be.server.coupon.controller.dto.CouponResponse;

import kr.hhplus.be.server.coupon.usecase.CouponIssueResult;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;



public class CouponControllerTest {


    @Test
    @DisplayName("쿠폰이 정상적으로 발급된다")
    void issueCoupon_success() throws Exception {
        // given
        long userId = 2L;
        long couponId = 100L;
        String message = "쿠폰 발급 성공";

        CouponIssuedUseCase fakeUseCase = command -> new CouponIssueResult(userId, couponId, message);
        CouponController controller = new CouponController(fakeUseCase);

        CouponRequest request = new CouponRequest(userId, couponId);

        // when
        CouponResponse response = controller.issueCoupon(request).getBody();

        // then
        Assertions.assertNotNull(response);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.couponId()).isEqualTo(couponId);
        assertThat(response.message()).isEqualTo(message);
    }

}
