package kr.hhplus.be.server.couponRedis.domain.service;



import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;
import kr.hhplus.be.server.couponRedis.domain.model.CouponUserJPA;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRedisQueueRepository;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRepository;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.couponRedis.domain.service.dto.Coupon;
import kr.hhplus.be.server.couponRedis.domain.service.dto.CouponUser;
import kr.hhplus.be.server.couponRedis.domain.service.dto.FinishResult;
import kr.hhplus.be.server.couponRedis.domain.service.dto.HoldResult;


import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
public class CouponIssuedService {


    private final CouponRepository couponRepo;
    private final CouponUserRepository couponUserRepo;
    private final CouponRedisQueueRepository couponRedisQueueRepo;

    public CouponIssuedService(CouponRepository couponRepository,
                               CouponUserRepository couponUserRepository,
                               CouponRedisQueueRepository couponRedisQueueRepo) {
        this.couponRepo = couponRepository;
        this.couponUserRepo = couponUserRepository;
        this.couponRedisQueueRepo = couponRedisQueueRepo;
    }

    @Transactional
    public CouponUser issueCouponToUser(Long couponId, Long userId, String reqId) {

        // 0) 정책 검증 (기간/활성 등)
        CouponJPA couponJPA = couponRepo.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰이 존재하지 않습니다."));
        Coupon coupon = couponJPA.toDomain();


        long now = System.currentTimeMillis();
        long limit = coupon.getTotalQuantity(); // 정책상 총량/잔여량 중 택1

        // 1) HOLD (원자: 큐 진입 + 컷 판정 + 예약)
        HoldResult hold = couponRedisQueueRepo.enqueueAndHold(couponId, userId, reqId, limit, now, 15_000);
        if (!hold.hold()) {
            if (hold.isAlreadyIssued()) throw new IllegalArgumentException("이미 발급된 사용자입니다.");
            if (hold.isOutOfCut()) throw new IllegalArgumentException("선착순 마감되었습니다.");
            throw new IllegalArgumentException("대기열 진입 실패: " + hold.error());
        }

        // 2) DB 확정 (최종 보증: UNIQUE (user_id,coupon_id) + (coupon_id,req_id))
        CouponUserJPA saved;
        try {
            saved = couponUserRepo.save(
                    CouponUserJPA.builder()
                            .userId(userId)
                            .couponId(couponId)
                            .reqId(reqId)
                            .isUsed(false)
                            .build()
            );
            // (선택) 카운터 증가: 동시성 경쟁에 안전한 단일 UPDATE
           couponRepo.incrementIssuedQuantity(couponId);

        } catch (DataIntegrityViolationException dup) {
            // 멱등/중복: 이미 발급됨으로 간주
            saved = couponUserRepo.findByCouponIdAndReqId(couponId, reqId)
                    .orElseGet(() -> couponUserRepo.findByUserIdAndCouponId(userId, couponId)
                            .orElseThrow(() -> dup));
        } catch (RuntimeException e) {
            // DB 실패 → 예약 취소
            couponRedisQueueRepo.finish(couponId, userId, reqId, false, System.currentTimeMillis());
            throw e;
        }

        // 3) COMMIT (원자: issued 반영 + 큐/예약 정리)
        FinishResult fin = couponRedisQueueRepo.finish(couponId, userId, reqId, true, System.currentTimeMillis());
        if (!fin.ok() && !"RESERVATION_EXPIRED".equals(fin.error())) {
            log.warn("commit anomaly couponId={}, userId={}, reqId={}, err={}",
                    couponId, userId, reqId, fin.error());
        }

        return saved.toDomain();
    }
}