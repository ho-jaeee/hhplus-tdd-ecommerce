package kr.hhplus.be.server.couponRedisKafka.usecase.dto;

public record CouponIssueResultNew(
        Long userId,
        Long couponId,
        boolean success,
        IssueResultCode code,
        String message,
        Long rank,
        Long couponUserId
) {
    public static CouponIssueResultNew ok(Long userId, Long couponId, Long rank, Long couponUserId) {
        return new CouponIssueResultNew(userId, couponId, true, IssueResultCode.SUCCESS, "OK", rank, couponUserId);
    }

    public static CouponIssueResultNew fail(Long userId, Long couponId, IssueResultCode code, String message) {
        return new CouponIssueResultNew(userId, couponId, false, code, message, null, null);
    }
}
