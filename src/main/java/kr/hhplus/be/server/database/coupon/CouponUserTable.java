package kr.hhplus.be.server.database.coupon;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CouponUserTable {

    private final Map<Long, CouponUserJPA> table = new HashMap<>();

    public CouponUserJPA insert(CouponUserJPA userCoupon) {
        table.put(userCoupon.getId(), userCoupon);
        return userCoupon;
    }

    public Optional<CouponUserJPA> findByCouponId(Long CouponId) {
        return Optional.ofNullable(table.get(CouponId));
    }

    public List<CouponUserJPA> findAll() {
        return new ArrayList<>(table.values());
    }

    public List<CouponUserJPA> findByUserId(Long userId) {
        return table.values().stream()
                .filter(c -> c.getUserId().equals(userId))
                .toList();
    }
}
