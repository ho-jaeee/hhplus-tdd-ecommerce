package kr.hhplus.be.server.unittest.couponTest;


import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CouponDiscountServiceTest {


    private CouponRepository couponRepository;
    private CouponDiscountService couponDiscountService;

    @BeforeEach
    void setUp() {
        couponRepository = mock(CouponRepository.class);
        couponDiscountService = new CouponDiscountService(couponRepository);
    }


    @Test
    @DisplayName("쿠폰할인이 정상적으로 반영된다.")
    void returnDiscountPercentage() {
        // given
        long couponId = 100L;
        int expectedDiscount = 15;

        Coupon coupon = new Coupon(
                couponId,
                "테스트 쿠폰",
                expectedDiscount,
                100,
                10,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now()
        );

        given(couponRepository.findByCouponId(couponId))
                .willReturn(Optional.of(coupon.toEntity()));

        // when
        int discount = couponDiscountService.getDiscountPercent(couponId);

        // then
        assertThat(discount).isEqualTo(expectedDiscount);

    }

}
