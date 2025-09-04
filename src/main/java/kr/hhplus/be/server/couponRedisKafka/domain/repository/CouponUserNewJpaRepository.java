package kr.hhplus.be.server.couponRedisKafka.domain.repository;


import kr.hhplus.be.server.couponRedisKafka.domain.model.CouponUserNewJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponUserNewJpaRepository extends JpaRepository<CouponUserNewJPA, Long> {
    Optional<CouponUserNewJPA> findByIdemKey(String idemKey);

    long countByIdemKey(String idemKey);

}
