package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.model.CouponUser;
import kr.hhplus.be.server.database.coupon.CouponUserTable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CouponUserRepositoryImpl implements CouponUserRepository {

    private final CouponUserTable couponUserTable;

    public CouponUserRepositoryImpl(CouponUserTable CouponUserTable) {

        this.couponUserTable = CouponUserTable;
    }

    @Override
    public CouponUser save(CouponUser couponUser) {

        CouponUserJPA entity = CouponUserJPA.from(couponUser);
        CouponUserJPA saved = couponUserTable.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<CouponUser> findByCouponId(Long couponId) {

        return couponUserTable.findByCouponId(couponId)
                .map(CouponUserJPA::toDomain);
    }

    @Override
    public List<CouponUser> findByUserId(Long userId) {

        return couponUserTable.findByUserId(userId).stream()
                .map(CouponUserJPA::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CouponUser> findByUserIdAndCouponId(Long userId, Long couponId) {
        return couponUserTable.findByUserIdAndCouponId(userId, couponId)
                .map(CouponUserJPA::toDomain);
    }

    @Override
    public List<CouponUser> findAll() {
        return couponUserTable.findAll().stream().
                map(CouponUserJPA::toDomain).collect(Collectors.toList());
    }
}

