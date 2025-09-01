package kr.hhplus.be.server.order.kafka;

import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import static org.springframework.kafka.support.KafkaHeaders.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    // 예: 데이터 플랫폼 적재/캐시집계 등을 담당할 서비스
    // private final DataPlatformIngestService ingestService;

    @KafkaListener(
            topics = "ecom.order.events",
            groupId = "dp.order.ingest"
    )
    public void onMessage(
            OrderPlacedKafka msg,
            @Header(RECEIVED_KEY) String key,
            @Header(RECEIVED_PARTITION) int partition,
            @Header(OFFSET) long offset
    ) {
        log.info("[consume] key={}, partition={}, offset={}, orderId={}, items={}",
                key, partition, offset, msg.orderId(), msg.items().size());

    }

}
