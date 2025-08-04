package kr.hhplus.be.server.database.coupon;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CouponTable {

    private final Map<Long, CouponJPA> table = new HashMap<>();


    public CouponJPA save(CouponJPA coupon) {
        table.put(coupon.getCouponId(), coupon);
        return coupon;
    }

    public Optional<CouponJPA> findByCouponId(Long couponId) {
        return Optional.ofNullable(table.get(couponId));
    }

    public List<CouponJPA> findAll() {
        return new ArrayList<>(table.values());
    }

}
