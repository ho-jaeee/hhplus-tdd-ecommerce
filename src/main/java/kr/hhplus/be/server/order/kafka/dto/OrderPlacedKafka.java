package kr.hhplus.be.server.order.kafka.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderPlacedKafka(
        Long orderId,
        Long userId,
        Long couponId,
        long totalPrice,
        long payPoint,
        List<OrderItemLine> items,
        LocalDateTime createdAt

)
{
    public static record OrderItemLine(
            Long productId,
            String productName,
            long pricePerUnit,
            int quantity
    ) {}
}
