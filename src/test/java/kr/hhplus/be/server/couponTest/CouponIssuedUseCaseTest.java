package kr.hhplus.be.server.couponTest;



import kr.hhplus.be.server.coupon.usecase.CouponIssueCommand;
import kr.hhplus.be.server.coupon.usecase.CouponIssueResult;
import kr.hhplus.be.server.coupon.domain.service.CouponUser;
import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCase;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class CouponIssuedUseCaseTest {

    @Mock
    private CouponIssuedService couponIssuedService;

    private CouponIssuedUseCase couponIssuedUseCase;

    @BeforeEach
    void setUp() {
        couponIssuedUseCase = new CouponIssuedUseCaseImpl(couponIssuedService);
    }

    @Test
    @DisplayName("쿠폰이 정상적으로 발급된다.")
    void issueCoupon() {


        // given
        long couponId = 100L;
        long userId = 2L;

        CouponUser couponUser = new CouponUser(
                userId,
                couponId,
                false,
                LocalDateTime.now(),
                null
        );


        CouponIssueCommand command = new CouponIssueCommand(userId, couponId);
        CouponIssueResult result = CouponIssueResult.success(couponId, userId);

        // 여기서만 mock 반환값 지정
        given(couponIssuedService.issueCouponToUser(couponId, userId))
                .willReturn(couponUser);


        // when
        CouponIssueResult expect = couponIssuedUseCase.issueCoupon(command);

        // then
        then(couponIssuedService).should(times(1))
                .issueCouponToUser(couponId, userId);


        // then
        assertThat(expect.message()).isEqualTo("쿠폰 발급 성공");
        assertThat(expect.userId()).isEqualTo(userId);
        assertThat(expect.couponId()).isEqualTo(couponId);

    }
}
