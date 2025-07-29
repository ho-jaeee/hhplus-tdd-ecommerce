package kr.hhplus.be.server.order.domain.model;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderItem{

    private final Long productId;
    private final String productName;
    private final long pricePerUnit;
    private final int quantity;
    private final long totalPrice;

    public static OrderItem create(Long productId, String productName,long pricePerUnit, int quantity)
    {   long total = quantity * pricePerUnit;
        return new OrderItem(productId, productName, pricePerUnit, quantity, total);
    }

    public OrderItemJPA toEntity(Long orderId) {
        return OrderItemJPA.builder()
                .orderId(orderId)
                .productId(productId)
                .productName(productName)
                .pricePerUnit(pricePerUnit)
                .quantity(quantity)
                .totalPrice(totalPrice) // 도메인에서 미리 계산된 total
                .build();
    }
}
