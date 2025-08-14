package kr.hhplus.be.server.coupon.usecase;

import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponIssuedUseCaseImpl implements CouponIssuedUseCase {

    private final CouponIssuedService couponIssuedService; // @Transactional in Service
    private final RedissonClient redisson;

    private static final long LOCK_WAIT_MS  = 5000;   // 시도 대기시간
    private static final long LOCK_LEASE_MS = 3000;  // 임차(자동해제) 시간

    @Override
    public CouponIssueResult issueCoupon(CouponIssueCommand command) {
        Long couponId = command.couponId();
        Long userId   = command.userId();

        if (couponId == null || couponId <= 0 || userId == null || userId <= 0) {
            return CouponIssueResult.fail(userId, couponId, "INVALID_ARGUMENT");
        }

        String lockKey = "lock:coupon:" + couponId;
        RLock lock = redisson.getLock(lockKey);
        boolean locked = false;

        try {
            // 분산락 선점
            locked = lock.tryLock(LOCK_WAIT_MS, LOCK_LEASE_MS, TimeUnit.MILLISECONDS);
            if (!locked) {
                return CouponIssueResult.fail(userId, couponId, "LOCK_TIMEOUT");
            }

            // 락이 잡힌 상태에서 트랜잭션 진입 (Service)
            CouponUser issued = couponIssuedService.issueCouponToUser(couponId, userId);

            // null 반환 정책이면 Optional로 감싸도 됨
            if (issued == null) {
                return CouponIssueResult.fail(userId, couponId, "쿠폰 발급 실패");
            }
            return CouponIssueResult.success(userId, couponId);

        } catch (DataIntegrityViolationException e) {
            // UNIQUE(user_id, coupon_id) 위반(중복 발급)
            return CouponIssueResult.fail(userId, couponId, "ALREADY_ISSUED");

        } catch (IllegalArgumentException e) {
            // 쿠폰 미존재 등 도메인 예외 매핑
            return CouponIssueResult.fail(userId, couponId, e.getMessage());

        } catch (Exception e) {
            // 그 외 알 수 없는 오류
            return CouponIssueResult.fail(userId, couponId, "UNKNOWN_ERROR");

        } finally {
            // 안전한 해제
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}