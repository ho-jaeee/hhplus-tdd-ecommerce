package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    CouponJPA insertOrUpdate(CouponJPA coupon);

    Optional<CouponJPA> findByCouponId(Long couponId);

    List<CouponJPA> findAll();
}
