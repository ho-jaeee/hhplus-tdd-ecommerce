package kr.hhplus.be.server.coupon.domain.service;



import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import kr.hhplus.be.server.coupon.policy.CouponValidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
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
    @Transactional
    public CouponUser issueCouponToUser(Long couponId, Long userId) {

        //쿠폰 존재유무 확인
        CouponJPA couponJPA = couponRepo.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));

        Coupon coupon = Coupon.fromEntity(couponJPA);

        //사용자에게 저장된 쿠폰 확인
        Optional<CouponUser> existingCoupon = couponUserRepo.findByUserIdAndCouponId(userId, couponId)
                .map(CouponUser::fromEntity);

        couponValidator.validateIssue(coupon, existingCoupon);

        //쿠폰 발급 수량증가 및 발급일자 업데이트
        Coupon increased = coupon.increaseIssuedQuantity();
        couponJPA.setIssuedQuantity(increased.getIssuedQuantity());
        couponJPA.setUpdatedAt(increased.getUpdatedAt());

        CouponUser issued = new CouponUser(userId, couponId, false, null, null);
        return CouponUser.fromEntity(
                couponUserRepo.save(issued.toEntity())
        );
    }
}
