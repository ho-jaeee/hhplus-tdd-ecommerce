package kr.hhplus.be.server.couponRedisKafka.adapter;

import kr.hhplus.be.server.couponRedisKafka.domain.repository.CouponNewJpaRepository;
import kr.hhplus.be.server.couponRedisKafka.port.CouponRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepositoryPort {

    private final CouponNewJpaRepository repo;


    @Override
    public boolean existsById(Long couponId) {
        return repo.existsById(couponId);
    }

    @Override
    @Transactional
    public boolean tryIncreaseIssued(Long couponId) {
        return repo.increaseIssuedIfAvailable(couponId) > 0;
    }
}
