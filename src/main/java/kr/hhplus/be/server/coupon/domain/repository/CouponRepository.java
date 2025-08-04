package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends CrudRepository<CouponJPA, Long> {

    Optional<CouponJPA> findByCouponId(Long couponId);

    List<CouponJPA> findAll();
}
