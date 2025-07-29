package kr.hhplus.be.server.order.controller.dto;

import kr.hhplus.be.server.order.usecase.OrderItemCommand;
import lombok.Builder;

@Builder
public record OrderItemRequest (
        Long productId,
        String productName,
        Long pricePerUnit,
        Integer quantity
) {
    public OrderItemCommand toCommand() {
        return new OrderItemCommand(
                productId,
                productName,
                pricePerUnit,
                quantity
        );
    }
}
