package kr.hhplus.be.server.coupon.infrastructure;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.model.Coupon;

import kr.hhplus.be.server.database.coupon.CouponTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {


    private final CouponTable couponTable;


    @Override
    public Coupon save(Coupon coupon) {
        CouponJPA entity = CouponJPA.from(coupon);     // 도메인 → JPA
        CouponJPA saved = couponTable.save(entity);
        return saved.toDomain();                       // JPA → 도메인
    }

    @Override
    public Optional<Coupon> findByCouponId(Long couponId) {
        return couponTable.findByCouponId(couponId)
                .map(CouponJPA::toDomain);           // Optional<CouponJPA> → Optional<Coupon>
    }

    @Override
    public List<Coupon> findAll() {
        return couponTable.findAll().stream()
                .map(CouponJPA::toDomain)
                .collect(Collectors.toList());
    }
}
