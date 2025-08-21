package kr.hhplus.be.server.coupon.infrastructure;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface SpringDataCouponRepository extends JpaRepository<CouponJPA, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update CouponJPA c 
              set c.issuedQuantity = c.issuedQuantity + 1,
                  c.updatedAt = CURRENT_TIMESTAMP
            where c.couponId = :couponId
           """)
    int incrementIssuedQuantity(@Param("couponId") Long couponId);
}
