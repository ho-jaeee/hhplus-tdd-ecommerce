package kr.hhplus.be.server.config.kafka;

import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;

    /** spring.json.* / (key|value).deserializer 등 충돌 유발 키 제거 */
    private static Map<String, Object> sanitized(Map<String, Object> src) {
        Map<String, Object> m = new HashMap<>(src);
        m.keySet().removeIf(k -> {
            String s = String.valueOf(k);
            return s.startsWith("spring.json.")
                    || "key.deserializer".equals(s)
                    || "value.deserializer".equals(s);
        });
        return m;
    }

    @Bean
    public ConsumerFactory<String, OrderPlacedKafka> orderConsumerFactory() {
        Map<String, Object> base = kafkaProperties.buildConsumerProperties(null);
        Map<String, Object> props = sanitized(base);

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "dp.order.ingest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JsonDeserializer<OrderPlacedKafka> jd = new JsonDeserializer<>(OrderPlacedKafka.class, false);
        jd.addTrustedPackages("kr.hhplus.be.server.order.kafka.dto");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), jd);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderPlacedKafka> orderListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderPlacedKafka> f =
                new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(orderConsumerFactory());

        // 주문은 RECORD 커밋 정책(예외 시 컨테이너 에러핸들러에 위임)
        f.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        FixedBackOff backOff = new FixedBackOff(1_000L, 2L);
        DefaultErrorHandler eh = new DefaultErrorHandler((rec, ex) -> {
            log.error("[order] consume failed topic={} partition={} offset={} key={} ex={}",
                    rec.topic(), rec.partition(), rec.offset(), rec.key(), ex.toString());
        }, backOff);


        f.setCommonErrorHandler(eh);
        return f;
    }
}
