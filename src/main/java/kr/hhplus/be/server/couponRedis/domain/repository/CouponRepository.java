package kr.hhplus.be.server.couponRedis.domain.repository;

import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;

import java.util.List;
import java.util.Optional;

public interface CouponRepository{

    Optional<CouponJPA> findById(Long couponId);

    CouponJPA save(CouponJPA couponJPA);

    List<CouponJPA> findAll();

    void deleteAll(); //테스트용

    int incrementIssuedQuantity(Long couponId);

}
