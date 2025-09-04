package kr.hhplus.be.server.couponRedis.domain.repository;


import kr.hhplus.be.server.couponRedis.domain.model.CouponUserJPA;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository{

    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);

    Optional<CouponUserJPA> findByCouponIdAndReqId(Long couponId, String reqId);

    Optional<CouponUserJPA> findByReqId(String reqId);

    long countByReqId(String reqId);

    List<CouponUserJPA> findAll();

    CouponUserJPA save(CouponUserJPA couponUserJPA);

    long count();

    void deleteAll(); // 테스트용
}
