package kr.hhplus.be.server.mockOrder;

public record orderResponse (
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        Long discountedTotalPrice,
        String message
){}
