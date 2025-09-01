package kr.hhplus.be.server.order.component;

import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
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
    public Long getCouponId()       { return order.getCouponId(); }
    public long getTotalPrice()     { return order.getTotalPrice(); }
    public LocalDateTime getOrderCreatedAt() { return order.getCreatedAt(); }


}
