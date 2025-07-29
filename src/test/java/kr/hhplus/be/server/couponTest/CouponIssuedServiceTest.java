package kr.hhplus.be.server.couponTest;

import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import kr.hhplus.be.server.coupon.domain.service.Coupon;
import kr.hhplus.be.server.coupon.domain.service.CouponUser;
import kr.hhplus.be.server.coupon.policy.CouponValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class CouponIssuedServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserRepository couponUserRepository;

    @Mock
    private CouponValidator couponValidator;

    @InjectMocks
    private CouponIssuedService couponIssuedService;



    @Test
    @DisplayName("정상적인 쿠폰 발급은 Test를 통과한다.")
    void couponIssued(){

        // given
        long couponId = 100L;
        long userId = 2L;

        Coupon coupon = new Coupon(
                couponId,
                "10% 할인쿠폰",
                10,
                100,
                10,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.of(coupon));
        given(couponUserRepository.findByUserIdAndCouponId(userId, couponId)).willReturn(Optional.empty());
        given(couponUserRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));


        // when
        CouponUser issued = couponIssuedService.issueCouponToUser(couponId, userId);


        // then
        assertThat(issued).isNotNull();
        assertThat(issued.getCouponId()).isEqualTo(couponId);
        assertThat(issued.getUserId()).isEqualTo(userId);
        assertThat(issued.isUsed()).isFalse();


        Mockito.verify(couponValidator).validateIssue(coupon, Optional.empty());
        Mockito.verify(couponUserRepository).save(any(CouponUser.class));

    }

}
