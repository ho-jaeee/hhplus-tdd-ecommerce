package kr.hhplus.be.server.coupon.controller.dto;

public record CouponRequest (
        Long userId,
        Long couponId
){
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID입니다.");
        }
        if (couponId == null || couponId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 쿠폰 ID입니다.");
        }
    }
}
