package kr.hhplus.be.server.order.controller.dto;

public record OrderResponse (
        Long orderId,
        String status,
        Long totalPrice,
        Long discountedPrice,
        String message
){}
