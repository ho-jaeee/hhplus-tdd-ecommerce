package kr.hhplus.be.server.order.component;

import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class OrderPlacedEvent {


    private final List<OrderItemCommand> items;
    private final Order order;
    private final long point;
    private final Instant createdAt;
    private final String eventId;


    private OrderPlacedEvent(
                             List<OrderItemCommand> items,
                             Order order,
                             long point,
                             Instant createdAt,
                             String eventId
    ) {
        this.items = items;
        this.order = order;
        this.point = point;
        this.createdAt = createdAt;
        this.eventId = eventId;
    }

    public static OrderPlacedEvent of(List<OrderItemCommand> items, Order order, long point) {
        return new OrderPlacedEvent(
                items,
                order,
                point,
                Instant.now(),
                UUID.randomUUID().toString()
        );
    }

    public Long getOrderId() { return order.getOrderId(); }
    public Long getUserId()  { return order.getUserId();  }

    /** 캐시 적재용 DTO 변환(수량=score) */
    public List<ProductPopularDto> toPopularDtos() {
        return items.stream()
                .map(it -> new ProductPopularDto(
                        it.productId(),
                        it.productName(),     // null 가능하면 조회단에서 fallback
                        it.quantity()         // score로 사용
                ))
                .collect(Collectors.toList());
    }
}
