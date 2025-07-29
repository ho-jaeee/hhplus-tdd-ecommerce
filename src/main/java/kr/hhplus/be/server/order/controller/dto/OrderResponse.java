package kr.hhplus.be.server.order.controller.dto;

import kr.hhplus.be.server.order.usecase.OrderResult;

import java.util.List;

public record OrderResponse (
        Long orderId,
        Long userId,
        Long totalPrice,
        Long discountedPrice,
        List<OrderItemResponse> items,
        String message
){
    public static OrderResponse from(OrderResult result) {

        List<OrderItemResponse> itemResponses = result.items() != null
                ? result.items().stream()
                .map(OrderItemResponse::from)
                .toList()
                : List.of();

        return new OrderResponse(
                result.orderId(),
                result.userId(),
                result.totalPrice(),
                result.discountedPrice(),
                itemResponses,
                result.status()
        );
    }
}
