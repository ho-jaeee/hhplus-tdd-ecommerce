package kr.hhplus.be.server.database.order;

import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OrderHistoryTable {

    private final Map<Long, OrderHistoryJPA> table = new HashMap<>();

    public OrderHistoryJPA insert(OrderHistoryJPA history) {
        throttle(300);
        table.put(history.getId(), history);
        return history;
    }

    public List<OrderHistoryJPA> findByOrderId(long orderId) {
        throttle(200);
        return table.values().stream()
                .filter(h -> h.getOrderId().equals(orderId))
                .toList();
    }

    private void throttle(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
        } catch (InterruptedException ignored) {

        }
    }
}
