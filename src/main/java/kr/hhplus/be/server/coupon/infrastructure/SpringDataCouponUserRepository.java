package kr.hhplus.be.server.coupon.infrastructure;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataCouponUserRepository extends JpaRepository<CouponUserJPA, Long> {
    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);

}
