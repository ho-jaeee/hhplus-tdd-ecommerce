package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponUserRepository extends JpaRepository<CouponUserJPA, Long> {

    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);

    List<CouponUserJPA> findAll();
}
