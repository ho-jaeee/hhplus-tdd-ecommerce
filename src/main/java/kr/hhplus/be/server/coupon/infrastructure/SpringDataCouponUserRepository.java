package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface SpringDataCouponUserRepository extends JpaRepository<CouponUserJPA, Long> {
    Optional<CouponUserJPA> findByUserIdAndCouponId(Long userId, Long couponId);
    Optional<CouponUserJPA> findByCouponIdAndReqId(Long couponId, String reqId);

    Optional<CouponUserJPA> findByReqId(String reqId);
    long countByReqId(String reqId);

}
