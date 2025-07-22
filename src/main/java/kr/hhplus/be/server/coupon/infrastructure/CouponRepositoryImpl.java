package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.database.coupon.CouponTable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponTable couponTable;

    public CouponRepositoryImpl(CouponTable couponTable) {
        this.couponTable = couponTable;
    }


    @Override
    public CouponJPA insertOrUpdate(CouponJPA coupon) {
        return couponTable.insertOrUpdate(coupon);
    }

    @Override
    public Optional<CouponJPA> findByCouponId(Long couponId) {
        return couponTable.findByCouponId(couponId);
    }

    @Override
    public List<CouponJPA> findAll() {
        return couponTable.findAll();
    }
}
