package kr.hhplus.be.server.coupon.usecase;


import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssuedUseCaseImpl implements CouponIssuedUseCase {

    private final CouponIssuedService couponIssuedService; // @Transactional in Service

    @DistributedLock(
            keys = { "'lock:couponissued:{' + #command.couponId + '}'" },
            waitMs = 5000,
            leaseMs = 0,           // 워치독 사용(권장)
            throwOnTimeout = true
    )
    @Override
    public CouponIssueResult issueCoupon(CouponIssueCommand command) {
        Long couponId = command.couponId();
        Long userId   = command.userId();

        if (couponId == null || couponId <= 0 || userId == null || userId <= 0) {
            return CouponIssueResult.fail(userId, couponId, "잘못된 입력이 있습니다.");
        }
        try {
            CouponUser issued = couponIssuedService.issueCouponToUser(couponId, userId);
            if (issued == null) {
                return CouponIssueResult.fail(userId, couponId, "쿠폰 발급 실패");
            }
            return CouponIssueResult.success(userId, couponId);

        } catch (IllegalArgumentException e) {
            return CouponIssueResult.fail(userId, couponId, e.getMessage());
        } catch (Exception e) {
            return CouponIssueResult.fail(userId, couponId, "UNKNOWN_ERROR");
        }
    }
}