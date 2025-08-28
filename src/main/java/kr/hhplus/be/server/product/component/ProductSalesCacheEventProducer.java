package kr.hhplus.be.server.product.component;

import kr.hhplus.be.server.common.event.DomainEventPublisher;
import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.product.component.dto.ProductSalesCacheBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class ProductSalesCacheEventProducer {


    private final DomainEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent e) {

        var occurred = e.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toInstant();

        var eventId = (e.getEventId() != null && !e.getEventId().isBlank())
                ? e.getEventId()
                : "order:" + e.getOrderId();

        var items = e.getItems().stream()
                .map(it -> ProductSalesCacheBatch.Item.builder()
                        .productId(it.productId())
                        .productName(it.productName())
                        .quantity((long) it.quantity())
                        .build())
                .toList();

        var batch = ProductSalesCacheBatch.builder()
                .items(items)
                .occurred(occurred)
                .eventId(eventId)
                .build();

        // AFTER_COMMIT 이후에 전용 이벤트 발행
        publisher.publish(new ProductSalesCacheEvent(batch));
    }
}
