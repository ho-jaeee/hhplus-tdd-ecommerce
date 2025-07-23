package kr.hhplus.be.server.order.controller.dto;

import lombok.Builder;

@Builder
public record OrderItemRequest (
        Long productId,
        String productName,
        Long pricePerUnit,
        Integer quantity
) {}
