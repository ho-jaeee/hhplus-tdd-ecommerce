package kr.hhplus.be.server.couponRedisKafka.domain.Service;

import kr.hhplus.be.server.couponRedisKafka.port.CouponRepositoryPort;
import kr.hhplus.be.server.couponRedisKafka.port.CouponUserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponIssueServiceNewImpl implements CouponIssueServiceNew{

    private final CouponRepositoryPort couponRepo;
    private final CouponUserRepositoryPort couponUserRepo;

    @Override
    @Transactional
    public IssueAttemptResult CouponIssueIdempotent(Long couponId, Long userId, String idemKey) {
        // 멱등키로 이미 발급된 경우(고유 제약으로 잡음) → 저장 시 예외 대신 기존 PK 반환하도록 어댑터에서 처리
        // 1) 재고 차감
        boolean ok = couponRepo.tryIncreaseIssued(couponId);
        if (!ok) {
            // 재고 없음 → 그래도 멱등키로 이미 발급된 케이스면 SUCCESS로 간주하게 할 수도 있음(정책)
            return new IssueAttemptResult(false, null, Reason.OUT_OF_STOCK);
        }
        // 2) 발급 행 저장(멱등키 UNIQUE)
        Long couponUserId = couponUserRepo.saveIssued(couponId, userId, idemKey);
        if (couponUserId == null) {
            // 멱등 중복으로 기존 레코드가 있었던 경우
            return new IssueAttemptResult(false, null, Reason.IDEMPOTENT_HIT);
        }
        return new IssueAttemptResult(true, couponUserId, Reason.SUCCESS);
    }
}

