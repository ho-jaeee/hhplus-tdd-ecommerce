package kr.hhplus.be.server.order.controller.dto;

import kr.hhplus.be.server.order.usecase.OrderCommand;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderRequest (
        Long userId,
        Long couponId, // nullable
        List<OrderItemRequest> items
) {
    public static OrderCommand toCommand(OrderRequest req) {
        return new OrderCommand(
                req.userId,
                req.couponId,
                req.items.stream()
                        .map(OrderItemRequest::toCommand)
                        .toList()
        );
    }
}
