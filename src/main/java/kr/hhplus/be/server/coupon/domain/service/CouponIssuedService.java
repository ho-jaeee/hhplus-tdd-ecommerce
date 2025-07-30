package kr.hhplus.be.server.coupon.domain.service;


import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import kr.hhplus.be.server.coupon.policy.CouponValidator;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class CouponIssuedService {


    private final CouponRepository couponRepo;
    private final CouponUserRepository couponUserRepo;
    private final CouponValidator couponValidator;

    public CouponIssuedService(CouponRepository couponRepository,
                               CouponUserRepository couponUserRepository,
                               CouponValidator couponValidator) {
        this.couponRepo = couponRepository;
        this.couponUserRepo = couponUserRepository;
        this.couponValidator = couponValidator;
    }

    public CouponUser issueCouponToUser(Long couponId, Long userId) {
        Coupon coupon = couponRepo.findByCouponId(couponId)
                .map(Coupon::fromEntity) // CouponJPA -> Coupon
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        Optional<CouponUser> existingCoupon = couponUserRepo.findByUserIdAndCouponId(userId, couponId)
                .map(CouponUser::fromEntity);
        couponValidator.validateIssue(coupon, existingCoupon);

        CouponUser issued = new CouponUser(userId, couponId, false, null, null);
        return CouponUser.fromEntity(
                couponUserRepo.save(issued.toEntity())
        );
    }
}
