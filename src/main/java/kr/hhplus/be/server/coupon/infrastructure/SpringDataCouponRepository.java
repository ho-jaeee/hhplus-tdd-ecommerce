package kr.hhplus.be.server.coupon.infrastructure;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataCouponRepository extends JpaRepository<CouponJPA, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponJPA c WHERE c.couponId = :couponId")
    Optional<CouponJPA> findByIdWithLock(@Param("couponId") Long couponId);
}
