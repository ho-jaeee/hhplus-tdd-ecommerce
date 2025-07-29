package kr.hhplus.be.server.coupon.usecase;

public record CouponIssueCommand (
        long userId,
        long couponId
) {
    public static CouponIssueCommand of(long userId, long couponId) {
        return new CouponIssueCommand(userId, couponId);
    }
}
