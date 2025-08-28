package kr.hhplus.be.server.order.usecase.dto;

import kr.hhplus.be.server.order.domain.service.dto.Order;

import java.util.List;


// usecase 출력 전용 DTO
public record OrderResult(Long orderId, Long userId, Long totalPrice, Long discountedPrice, List<OrderItemResult> items,
                          String status) {

    public static OrderResult of(Order savedOrder, List<OrderItemCommand> items) {
        List<OrderItemResult> itemResults = items.stream()
                .map(i -> new OrderItemResult(
                        i.productId(),
                        i.productName(),
                        i.quantity(),
                        i.pricePerUnit() * i.quantity()
                ))
                .toList();

        return new OrderResult(
                savedOrder.getOrderId(),
                savedOrder.getUserId(),
                savedOrder.getTotalPrice(),
                savedOrder.getDiscountedTotalPrice(),
                itemResults,
                savedOrder.getStatus().name()
        );
    }


}

