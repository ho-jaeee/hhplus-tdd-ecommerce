package kr.hhplus.be.server;

public record userCouponRespone(
        Long couponId,
        String couponName,
        boolean isUsed,
        String issuedAt,
        String usedAt
){}

