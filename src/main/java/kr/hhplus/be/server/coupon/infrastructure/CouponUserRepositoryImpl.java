package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
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
    public List<CouponUserJPA> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public CouponUserJPA save(CouponUserJPA couponUserJPA) {
        return jpaRepository.save(couponUserJPA);
    }
}
