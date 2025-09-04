package kr.hhplus.be.server.couponRedisKafka.adapter;

import kr.hhplus.be.server.couponRedisKafka.domain.model.CouponUserNewJPA;
import kr.hhplus.be.server.couponRedisKafka.domain.repository.CouponUserNewJpaRepository;
import kr.hhplus.be.server.couponRedisKafka.port.CouponUserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CouponUserRepositoryAdapter implements CouponUserRepositoryPort {


    private final CouponUserNewJpaRepository repo;


    @Override
    @Transactional
    public Long saveIssued(Long couponId, Long userId, String idemKey) {
        try {
            CouponUserNewJPA cu = new CouponUserNewJPA();
            cu.setCouponId(couponId);
            cu.setUserId(userId);
            cu.setIdemKey(idemKey);
            cu.setCreatedAt(Instant.now());
            CouponUserNewJPA saved = repo.save(cu);      // JPA가 PK 채워서 반환
            return saved.getId();
        } catch (DataIntegrityViolationException e) {
            // UNIQUE(idem_key) 충돌 → 기존 레코드 PK 반환
            return repo.findByIdemKey(idemKey).map(CouponUserNewJPA::getId).orElse(null);
        }
    }
}
