package kr.hhplus.be.server.coupon.usecase;

public record CouponIssueResult(
        long userId,
        long couponId,
        String message
) {
    public static CouponIssueResult success(long userId, long couponId) {
        return new CouponIssueResult(userId, couponId, "쿠폰 발급 성공");
    }

    public static CouponIssueResult fail(long userId, long couponId, String message) {
        return new CouponIssueResult(userId, couponId, message);
    }
}
