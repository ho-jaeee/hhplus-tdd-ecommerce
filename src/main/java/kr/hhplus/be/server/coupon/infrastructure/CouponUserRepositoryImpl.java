package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.database.coupon.CouponUserTable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CouponUserRepositoryImpl implements CouponUserRepository {

    private final CouponUserTable couponUserTable;

    public CouponUserRepositoryImpl(CouponUserTable CouponUserTable) {
        this.couponUserTable = CouponUserTable;
    }


    @Override
    public CouponUserJPA insert(CouponUserJPA couponUser) {
        return couponUserTable.insert(couponUser);
    }

    @Override
    public Optional<CouponUserJPA> findByCouponId(Long CouponId) {
        return couponUserTable.findByCouponId(CouponId);
    }

    @Override
    public List<CouponUserJPA> findByUserId(Long userId) {
        return couponUserTable.findByUserId(userId);
    }

    @Override
    public List<CouponUserJPA> findAll() {
        return couponUserTable.findAll();
    }
}
