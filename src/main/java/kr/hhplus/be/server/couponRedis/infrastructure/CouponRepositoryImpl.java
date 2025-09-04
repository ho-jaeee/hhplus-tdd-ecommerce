package kr.hhplus.be.server.couponRedis.infrastructure;

import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponRepositoryImpl implements CouponRepository {

    private final SpringDataCouponRepository jpaRepository;

    public CouponRepositoryImpl(SpringDataCouponRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<CouponJPA> findById(Long couponId) {
        return jpaRepository.findById(couponId);
    }

    @Override
    public CouponJPA save(CouponJPA couponJPA) {
        return jpaRepository.save(couponJPA);
    }

    @Override
    public List<CouponJPA> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public int incrementIssuedQuantity(Long couponId) {
        return jpaRepository.incrementIssuedQuantity(couponId);
    }


}
