package kr.hhplus.be.server.config.kafka;


import kr.hhplus.be.server.couponRedisKafka.event.IssueRequestEvent;
import kr.hhplus.be.server.couponRedisKafka.event.IssueResultEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class CouponKafkaProducerConfig {


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        // 멱등성/재시도 기본값 보강
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // EOS 안전 범위
        // 필요 시 성능 튜닝
        // props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        // props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);

        // 타입 헤더 제거(이종 컨슈머 호환)
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return props;
    }

    @Bean
    public ProducerFactory<String, IssueRequestEvent> issueReqProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProps(), new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, IssueRequestEvent> issueReqKafkaTemplate() {
        return new KafkaTemplate<>(issueReqProducerFactory());
    }

    @Bean
    public ProducerFactory<String, IssueResultEvent> issueResProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProps(), new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, IssueResultEvent> issueResKafkaTemplate() {
        return new KafkaTemplate<>(issueResProducerFactory());
    }
}
