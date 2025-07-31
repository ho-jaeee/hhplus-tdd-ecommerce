package kr.hhplus.be.server.integrationTest.couponTest;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CouponDiscountServiceTestInte {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponDiscountService couponDiscountService;

    private CouponJPA savedCoupon;

    @BeforeEach
    void setUp() {
        // given
        savedCoupon = couponRepository.save(
                new CouponJPA(
                        null,                     // couponId
                        "통합테스트 쿠폰",         // name
                        15,                       // discountPercent
                        100,                      // totalQuantity
                        10,                       // issuedQuantity
                        LocalDateTime.now().minusDays(1), // startAt
                        LocalDateTime.now().plusDays(1),  // endAt
                        LocalDateTime.now().minusDays(2), // createdAt
                        LocalDateTime.now()              // updatedAt
                )
        );
    }

    @Test
    @DisplayName("CouponDiscountService: 쿠폰 할인율 정상 조회")
    void getDiscountPercent_returnsCorrectDiscount() {
        // when
        int discount = couponDiscountService.getDiscountPercent(savedCoupon.getCouponId());

        // then
        assertThat(discount).isEqualTo(15);
    }
}
