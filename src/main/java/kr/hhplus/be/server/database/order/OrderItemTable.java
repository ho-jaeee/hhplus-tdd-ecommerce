package kr.hhplus.be.server.database.order;

import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OrderItemTable {
    private final Map<Long, OrderItemJPA> table = new HashMap<>();

    public OrderItemJPA insert(OrderItemJPA item) {
        throttle(300);
        table.put(item.getId(), item);
        return item;
    }

    public List<OrderItemJPA> findByOrderId(long orderId) {
        throttle(200);
        return table.values().stream()
                .filter(i -> i.getOrderId().equals(orderId))
                .toList();
    }

    public OrderItemJPA selectById(long Id) {
        throttle(200);
        return table.get(Id);
    }
    private void throttle(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
        } catch (InterruptedException ignored) {

        }
    }
}
