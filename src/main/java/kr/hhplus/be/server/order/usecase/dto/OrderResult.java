package kr.hhplus.be.server.order.usecase.dto;

import java.util.List;


// usecase 출력 전용 DTO
public record OrderResult(
        Long orderId,
        Long userId,
        Long totalPrice,
        Long discountedPrice,
        List<OrderItemResult> items,
        String status
){}
