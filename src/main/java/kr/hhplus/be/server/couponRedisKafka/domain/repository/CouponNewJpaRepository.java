package kr.hhplus.be.server.couponRedisKafka.domain.repository;

import kr.hhplus.be.server.couponRedisKafka.domain.model.CouponNewJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponNewJpaRepository extends JpaRepository<CouponNewJPA, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
       update CouponNewJPA c
                                        set c.issued = c.issued + 1,
                                            c.updatedAt = CURRENT_TIMESTAMP
                                      where c.id = :couponId
                                        and c.issued < c.total
    """)
    int increaseIssuedIfAvailable(@Param("couponId") Long couponId);

}
