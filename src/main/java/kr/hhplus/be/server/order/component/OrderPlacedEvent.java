package kr.hhplus.be.server.order.component;

import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.service.dto.OrderItem;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderPlacedEvent {


    private final List<OrderItemCommand> items;
    private final Order order;
    private final long point;
    private final LocalDateTime createdAt;

    private OrderPlacedEvent(List<OrderItemCommand> items, Order order, long point) {
        this.items = items;
        this.order = order;
        this.point = point;
        this.createdAt = LocalDateTime.now();
    }

    public static OrderPlacedEvent of( List<OrderItemCommand> items, Order order, long point) {
        return new OrderPlacedEvent(items, order, point);
    }

    public Long getOrderId() { return order.getOrderId(); }
    public Long getUserId()  { return order.getUserId();  }
}
