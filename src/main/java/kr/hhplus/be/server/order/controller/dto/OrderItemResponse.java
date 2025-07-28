package kr.hhplus.be.server.order.controller.dto;

import kr.hhplus.be.server.order.domain.model.OrderItemResult;

public record OrderItemResponse(
        Long productId,
        String productName,
        int quantity,
        long totalPrice
) {
    public static OrderItemResponse from(OrderItemResult result) {
        return new OrderItemResponse(
                result.productId(),
                result.productName(),
                result.quantity(),
                result.totalPrice()
        );
    }
}