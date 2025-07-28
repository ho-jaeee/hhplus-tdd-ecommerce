package kr.hhplus.be.server.order.domain.model;


// usecase 출력 전용 DTO
public record OrderItemResult(
        Long productId,
        String productName,
        int quantity,
        long totalPrice
) {}
