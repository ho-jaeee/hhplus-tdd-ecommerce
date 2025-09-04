package kr.hhplus.be.server.couponRedis.infrastructure;


import kr.hhplus.be.server.couponRedis.domain.model.CouponUserJPA;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponUserRepositoryImpl implements CouponUserRepository {

    private final SpringDataCouponUserRepository jpaRepository;

    public CouponUserRepositoryImpl(SpringDataCouponUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId) {
        return jpaRepository.findByUserIdAndCouponId(userId, couponId);
    }

    @Override
    public Optional<CouponUserJPA> findByCouponIdAndReqId(Long couponId, String reqId) {
        return jpaRepository.findByCouponIdAndReqId(couponId, reqId);
    }

    @Override
    public Optional<CouponUserJPA> findByReqId(String reqId) {
        return jpaRepository.findByReqId(reqId);
    }

    @Override
    public long countByReqId(String reqId) {
        return jpaRepository.countByReqId(reqId);
    }

    @Override
    public List<CouponUserJPA> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public CouponUserJPA save(CouponUserJPA couponUserJPA) {
        return jpaRepository.save(couponUserJPA);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }


}
