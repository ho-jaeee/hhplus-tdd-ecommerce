package kr.hhplus.be.server.coupon.domain.repository;

import kr.hhplus.be.server.coupon.domain.service.dto.FinishResult;
import kr.hhplus.be.server.coupon.domain.service.dto.HoldResult;

public interface CouponRedisQueueRepository {
    HoldResult enqueueAndHold(Long couponId, Long userId, String reqId, long limit, long nowMillis, long ttlMillis);
    FinishResult finish(Long couponId, Long userId, String reqId,
                        boolean commit, long nowMillis);
}
