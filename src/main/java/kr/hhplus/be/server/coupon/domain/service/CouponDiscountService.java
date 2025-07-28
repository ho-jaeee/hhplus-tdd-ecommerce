package kr.hhplus.be.server.coupon.domain.service;

import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.model.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponDiscountService {

    private final CouponRepository couponRepository;

    public int getDiscountPercent(Long couponId) {
        return couponRepository.findByCouponId(couponId)
                .map(Coupon::getDiscountAmount)
                .orElse(0);  // 존재하지 않으면 0%
    }
}
