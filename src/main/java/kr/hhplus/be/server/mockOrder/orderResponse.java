package kr.hhplus.be.server.order;

public record orderResponse (
        Long orderId,
        OrderStatus status,
        Long totalPrice,
        Long discountedTotalPrice,
        String message
){}
