package kr.hhplus.be.server.coupon.controller.dto;

public record CouponResponse (
        Long userId,
        Long couponId,
        String message

){
    public static CouponResponse success(Long userId, Long couponId) {
        return new CouponResponse(userId, couponId, "쿠폰 발급 성공");
    }

    public static CouponResponse fail(Long userId, Long couponId, String reason) {
        return new CouponResponse(userId, couponId, reason);
    }
}
