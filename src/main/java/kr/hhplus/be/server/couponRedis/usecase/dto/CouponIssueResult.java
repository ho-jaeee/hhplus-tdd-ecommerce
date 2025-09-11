package kr.hhplus.be.server.couponRedis.usecase.dto;

public record CouponIssueResult(
        Long userId,
        Long couponId,
        boolean success,
        String message,
        Long rank
) {
    public static CouponIssueResult ok(Long u, Long c, Long r)     {
        return new CouponIssueResult(u, c, true,  "OK", r);
    }
    public static CouponIssueResult fail(Long u, Long c, String m) {
        return new CouponIssueResult(u, c, false, m, null);
    }


    public boolean isSuccess() {
        return success;
    }

}
