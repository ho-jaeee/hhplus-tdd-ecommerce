package kr.hhplus.be.server.order.kafka;

import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderPlacedAfterCommitHandler {

    private final OrderEventProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onKafka(OrderPlacedEvent e) {
        // OrderPlacedEvent -> OrderPlacedKafka 매핑
        OrderPlacedKafka payload = new OrderPlacedKafka(
                e.getOrderId(),                 // savedOrder.getOrderId()
                e.getUserId(),                  // savedOrder.getUserId()
                e.getCouponId(),                // savedOrder.getCouponId()
                e.getTotalPrice(),              // totalPrice
                e.getPoint(),                // payPoint
                e.getItems().stream()
                        .map(it -> new OrderPlacedKafka.OrderItemLine(
                                it.productId(), it.productName(),
                                it.pricePerUnit(), it.quantity()
                        ))
                        .toList(),
                e.getOrderCreatedAt()                // savedOrder.getCreatedAt() (or now)
        );

        producer.send(payload);
    }
}
