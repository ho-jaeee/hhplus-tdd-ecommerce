package kr.hhplus.be.server.coupon.domain.service;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponDiscountService {

    private final CouponRepository couponRepository;

    public int getDiscountPercent(Long couponId) {
        return couponRepository.findByCouponId(couponId)
                .map(CouponJPA::getDiscountAmount)
                .orElse(0);  // 존재하지 않으면 0%
    }
}
