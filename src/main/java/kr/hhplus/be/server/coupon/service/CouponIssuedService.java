package kr.hhplus.be.server.coupon.service;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponIssuedService {


    private final CouponRepository couponRepo;
    private final CouponUserRepository couponUserRepo;

    public CouponIssuedService(CouponRepository couponRepo, CouponUserRepository couponUserRepo) {
        this.couponRepo = couponRepo;
        this.couponUserRepo = couponUserRepo;
    }

    public CouponUserJPA issueCouponToUser(Long couponId, Long userId) {
        CouponJPA coupon = couponRepo.findByCouponId(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        CouponUserJPA issued = new CouponUserJPA(null, userId, couponId, false, null, null);
        return couponUserRepo.insert(issued);
    }
}
