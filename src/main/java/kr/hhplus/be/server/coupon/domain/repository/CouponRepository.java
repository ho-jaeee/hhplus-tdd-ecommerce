package kr.hhplus.be.server.coupon.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponRepository{

    Optional<CouponJPA> findById(Long couponId);

    CouponJPA save(CouponJPA couponJPA);

    List<CouponJPA> findAll();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponJPA c WHERE c.couponId = :couponId")
    Optional<CouponJPA> findByIdWithLock(@Param("couponId") Long couponId);
}
