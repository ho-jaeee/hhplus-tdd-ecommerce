package kr.hhplus.be.server.integrationTest.kafkaTest;


import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class KafkaSmokeTest {

    private static final String TOPIC = "smoke.test.topic";

    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers;

    KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() throws Exception {
        // 1) 토픽 보장
        Properties adminProps = new Properties();
        adminProps.put("bootstrap.servers", bootstrapServers);
        try (AdminClient admin = AdminClient.create(adminProps)) {
            var topics = admin.listTopics().names().get();
            if (!topics.contains(TOPIC)) {
                admin.createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1))).all().get();
            }
        }

        // 2) 프로듀서 준비
        Map<String, Object> prodProps = new HashMap<>();
        prodProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        prodProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        prodProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 안정 옵션(선택)
        prodProps.put(ProducerConfig.ACKS_CONFIG, "all");
        prodProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(prodProps));
    }

    @Test
    @DisplayName("Kafka 스모크: produce → consume")
    void produce_then_consume() {
        // given
        String key = "smoke-" + ThreadLocalRandom.current().nextInt(1_000_000);
        String expected = "hello-smoke-" + UUID.randomUUID();

        // when: 발행
        try {
            kafkaTemplate.send(TOPIC, key, expected).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Kafka send 실패", e);
        }

        // then: 임시 컨슈머로 폴링하여 수신 확인
        Properties consProps = new Properties();
        consProps.put("bootstrap.servers", bootstrapServers);
        consProps.put(ConsumerConfig.GROUP_ID_CONFIG, "smoke-test-consumer-" + UUID.randomUUID());
        consProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consProps)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            String actual = null;
            long deadline = System.currentTimeMillis() + 5000; // 최대 5초 대기
            while (System.currentTimeMillis() < deadline && actual == null) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
                if (!records.isEmpty()) {
                    actual = records.iterator().next().value();
                }
            }

            assertThat(actual)
                    .as("도커 Kafka(%s)에 발행한 메시지를 5초 내에 수신해야 합니다.", bootstrapServers)
                    .isEqualTo(expected);
        }
    }
}
