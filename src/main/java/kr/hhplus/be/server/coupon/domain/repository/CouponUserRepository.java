package kr.hhplus.be.server.coupon.domain.repository;


import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository{

    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);

    List<CouponUserJPA> findAll();

    CouponUserJPA save(CouponUserJPA couponUserJPA);

    long count();
}
