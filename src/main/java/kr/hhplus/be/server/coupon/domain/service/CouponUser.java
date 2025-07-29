package kr.hhplus.be.server.coupon.domain.service;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CouponUser {
    // Getter only
    private final Long userId;
    private final Long couponId;
    private final boolean isUsed;
    private final LocalDateTime issuedAt;
    private final LocalDateTime usedAt;

    public CouponUser(Long userId, Long couponId, boolean isUsed, LocalDateTime issuedAt, LocalDateTime usedAt) {
        this.userId = userId;
        this.couponId = couponId;
        this.isUsed = isUsed;
        this.issuedAt = issuedAt;
        this.usedAt = usedAt;
    }

    public static CouponUser issue(Long userId, Long couponId) {
        return new CouponUser(userId, couponId, false, LocalDateTime.now(), null);
    }

    public CouponUser markAsUsed() {
        return new CouponUser(userId, couponId, true, issuedAt, LocalDateTime.now());
    }

}
