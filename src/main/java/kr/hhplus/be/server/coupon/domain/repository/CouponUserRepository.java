package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository {

    CouponUserJPA insert(CouponUserJPA couponUser);

    Optional<CouponUserJPA> findByCouponId(Long CouponId);

    List<CouponUserJPA> findByUserId(Long userId);

    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);

    List<CouponUserJPA> findAll();
}
