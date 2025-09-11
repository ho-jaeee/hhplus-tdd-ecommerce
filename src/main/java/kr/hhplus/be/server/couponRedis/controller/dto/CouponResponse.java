package kr.hhplus.be.server.couponRedis.controller.dto;

public record CouponResponse (
        Long userId,
        Long couponId,
        boolean success,
        String message,
        Long rank    // 대기열 순번(옵션, 없으면 null)

){}
