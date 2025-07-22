package kr.hhplus.be.server.database.order;

import kr.hhplus.be.server.order.domain.model.OrderJPA;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OrderTable {

    private final Map<Long, OrderJPA> table = new HashMap<>();

    public OrderJPA insert(OrderJPA order) {
        throttle(300);
        table.put(order.getOrderId(), order);
        return order;
    }

    public void update(OrderJPA order) {
        throttle(200);
        table.put(order.getOrderId(), order);
    }


    public OrderJPA findByOrderId(long orderId) {
        throttle(200);
        return table.get(orderId);
    }

    private void throttle(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
        } catch (InterruptedException ignored) {

        }
    }

}
