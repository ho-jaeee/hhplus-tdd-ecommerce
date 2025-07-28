package kr.hhplus.be.server.couponTest;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.coupon.service.CouponIssuedService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CouponIssuedServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserRepository couponUserRepository;


    @Test
    @DisplayName("정상적인 쿠폰 발급은 Test를 통과한다")
    void couponIssued(){
        //given
        Long couponId = 1L;
        Long userId = 2L;
        CouponIssuedService service = new CouponIssuedService(couponRepository, couponUserRepository);


        CouponJPA coupon = new CouponJPA(
                couponId,
                "10% 할인쿠폰",
                10,
                100,
                50,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(couponRepository.findByCouponId(couponId)).willReturn(Optional.of(coupon));
        given(couponUserRepository.insert(any())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CouponUserJPA issuedCoupon = service.issueCouponToUser(couponId, userId);

        // then
        assertThat(issuedCoupon).isNotNull();
        assertThat(issuedCoupon.getCouponId()).isEqualTo(couponId);
        assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
        assertThat(issuedCoupon.getIsUsed()).isFalse();

        verify(couponRepository).findByCouponId(couponId);
        verify(couponUserRepository).insert(any(CouponUserJPA.class));


    }

}
