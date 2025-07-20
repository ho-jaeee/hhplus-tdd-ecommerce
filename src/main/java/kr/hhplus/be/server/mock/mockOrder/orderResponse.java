package kr.hhplus.be.server.mock.mockOrder;

public record orderResponse (
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        Long discountedTotalPrice,
        String message
){}
