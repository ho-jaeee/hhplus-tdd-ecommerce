package kr.hhplus.be.server.mockOrder;

public record orderRequest(
        Long userId,
        Long productId,
        int quantity,
        Long couponId // null 가능
) {}

