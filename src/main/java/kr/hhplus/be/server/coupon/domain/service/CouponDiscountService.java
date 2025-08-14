package kr.hhplus.be.server.coupon.domain.service;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponDiscountService {

    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;



    public int useCoupon(Long userId, Long couponId) {
        System.out.println("트랜잭션 활성 여부: " + TransactionSynchronizationManager.isActualTransactionActive());

       CouponUserJPA couponUserJPA = couponUserRepository.findByUserIdAndCouponId(userId, couponId)
               .orElseThrow(() -> new IllegalArgumentException("해당 쿠폰이 존재하지 않습니다."));

      if (Boolean.TRUE.equals(couponUserJPA.getIsUsed())) {
           throw new IllegalStateException("이미 사용된 쿠폰입니다.");
      }
        couponUserJPA.setIsUsed(true);
        couponUserJPA.setUsedAt(LocalDateTime.now());

        return couponRepository.findById(couponId)
                .map(Coupon::fromEntity)
                .map(Coupon::getDiscountAmount)
                .orElse(0);  // 존재하지 않으면 0%
   }


}


