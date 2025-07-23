package kr.hhplus.be.server.order.controller.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record OrderRequest (
        Long userId,
        Long couponId, // nullable
        List<OrderItemRequest> items
){}
