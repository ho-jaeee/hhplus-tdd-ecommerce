package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.service.CouponUser;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository {

    CouponUser save(CouponUser couponUser);

    Optional<CouponUser> findByCouponId(Long CouponId);

    List<CouponUser> findByUserId(Long userId);

    Optional<CouponUser> findByUserIdAndCouponId(Long userId, Long couponId);

    List<CouponUser> findAll();
}
