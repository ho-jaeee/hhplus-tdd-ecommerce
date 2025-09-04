package kr.hhplus.be.server.order.kafka;

import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    public static final String TOPIC = "ecom.order.events";
    private final KafkaTemplate<String, OrderPlacedKafka> kafkaTemplate;

    public void send(OrderPlacedKafka payload) {
        var key = String.valueOf(payload.orderId());
        var msg = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader("eventType", "OrderPlaced")
                .setHeader("schemaVersion", "1")
                .build();
        kafkaTemplate.send(msg);
    }
}
