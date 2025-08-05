package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCouponRepository extends JpaRepository<CouponJPA, Long> {
}
