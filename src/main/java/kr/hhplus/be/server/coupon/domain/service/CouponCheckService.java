package kr.hhplus.be.server.coupon.domain.service;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponCheckService {

    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;

    public void checkCoupon(long userId, long couponId) {
        CouponJPA coupon = couponRepository.findByCouponId(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        if (LocalDateTime.now().isBefore(coupon.getStartAt()) ||
                LocalDateTime.now().isAfter(coupon.getEndAt())) {
            throw new IllegalStateException("쿠폰 사용기간이 아닙니다.");
        }

     CouponUserJPA CouponUser = couponUserRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("사용자는 이 쿠폰을 보유하고 있지 않습니다."));

        if (CouponUser.getIsUsed()) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
    }

}
