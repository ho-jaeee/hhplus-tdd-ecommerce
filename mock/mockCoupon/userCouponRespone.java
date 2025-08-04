package kr.hhplus.be.server.mock.mockCoupon;

public record userCouponRespone(
        Long couponId,
        String couponName,
        boolean isUsed,
        String issuedAt,
        String usedAt
){}

