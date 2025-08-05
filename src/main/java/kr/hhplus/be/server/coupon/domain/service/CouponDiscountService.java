package kr.hhplus.be.server.coupon.domain.service;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponDiscountService {

    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;

    public int getDiscountPercent(Long couponId) {
        return couponRepository.findById(couponId)
                .map(Coupon::fromEntity)
                .map(Coupon::getDiscountAmount)
                .orElse(0);  // 존재하지 않으면 0%
    }

    public void useCoupon(Long userId, Long couponId) {
       CouponUserJPA couponUser = couponUserRepository.findByUserIdAndCouponId(userId, couponId)
               .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰이 존재하지 않습니다."));

      if (Boolean.TRUE.equals(couponUser.getIsUsed())) {
           throw new IllegalStateException("이미 사용된 쿠폰입니다.");
      }
       couponUser.setIsUsed(true);
      couponUser.setUsedAt(LocalDateTime.now());
   }


}


