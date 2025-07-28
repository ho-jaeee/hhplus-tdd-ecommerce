package kr.hhplus.be.server.order.domain.model;

import java.util.List;


// usecase 입력 전용 DTO
public record OrderCommand(
        Long userId,
        Long couponId,
        List<OrderItemCommand> items
){}
