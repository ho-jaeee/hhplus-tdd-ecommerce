package kr.hhplus.be.server.order.usecase;


// usecase 입력 전용 DTO
public record OrderItemCommand(
        Long productId,
        String productName,
        Long pricePerUnit,
        int quantity
){}
