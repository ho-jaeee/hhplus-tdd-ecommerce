package kr.hhplus.be.server.order.kafka;

import kr.hhplus.be.server.order.domain.service.OrderKafkaService;
import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.KafkaHeaders.*;

@Slf4j
@Component("orderEventConsumer")
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderKafkaService orderKafkaService;

    @KafkaListener(
            id = "order-consumer",
            topics = "ecom.order.events",
            groupId = "dp.order.ingest"

    )
    public void onMessage(
            OrderPlacedKafka msg,
            @Header(value = RECEIVED_KEY, required = false) String key,
            @Header(RECEIVED_PARTITION) int partition,
            @Header(OFFSET) long offset
    ) {
        log.info("[consume] key={}, partition={}, offset={}, orderId={}, items={}",
                key, partition, offset, msg.orderId(), msg.items().size());

        orderKafkaService.save(msg);

    }

}
