package kr.hhplus.be.server.order;

public record orderRequest(
        Long userId,
        Long productId,
        int quantity,
        Long couponId // null 가능
) {}

