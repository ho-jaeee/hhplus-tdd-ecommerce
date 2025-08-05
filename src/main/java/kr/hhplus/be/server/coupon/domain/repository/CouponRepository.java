package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;

import java.util.List;
import java.util.Optional;

public interface CouponRepository{

    Optional<CouponJPA> findById(Long couponId);

    CouponJPA save(CouponJPA couponJPA);

    List<CouponJPA> findAll();
}
