package kr.hhplus.be.server.couponTest;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CouponDiscountServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponDiscountService couponDiscountService;

    void returnDiscountPercentage() {
        long couponId = 100L;
        int expectedDiscount = 15;

        CouponJPA coupon = CouponJPA.builder()
                .couponId(couponId)
                .discountAmount(expectedDiscount)
                .build();

        given(couponRepository.findByCouponId(couponId))
                .willReturn(java.util.Optional.of(coupon));

        // when
        int result = couponDiscountService.getDiscountPercent(couponId);

        // then
        assertThat(result).isEqualTo(expectedDiscount);

    }

}
