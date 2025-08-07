package kr.hhplus.be.server.coupon.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository{

    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);

    List<CouponUserJPA> findAll();

    CouponUserJPA save(CouponUserJPA couponUserJPA);

    long count();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponUserJPA c WHERE c.userId = :userId AND c.couponId = :couponId")
    Optional<CouponUserJPA>  findByUserIdAndCouponIdForUpdate(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
