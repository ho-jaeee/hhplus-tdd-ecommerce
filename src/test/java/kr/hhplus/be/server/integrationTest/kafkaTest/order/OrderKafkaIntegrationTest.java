package kr.hhplus.be.server.integrationTest.kafkaTest.order;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.order.domain.model.OrderKafkaItemJPA;
import kr.hhplus.be.server.order.domain.repository.OrderKafkaItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderKafkaRepository;
import kr.hhplus.be.server.order.domain.service.OrderKafkaService;
import kr.hhplus.be.server.order.domain.service.OrderTransactionServiceImpl;
import kr.hhplus.be.server.order.kafka.OrderEventConsumer;
import kr.hhplus.be.server.order.kafka.OrderEventProducer;
import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;





import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;


import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderKafkaIntegrationTest {
    private static final String TOPIC = "ecom.order.events";

    // TestcontainersConfiguration 가 setProperty 한 값을 그대로 사용
    @Value("${spring.kafka.bootstrap-servers}")
    String bootstrapServers;

    @Autowired
    KafkaTemplate<String, OrderPlacedKafka> kafkaTemplate;

    //사용자 포인트 충전
    @Autowired
    UserPointRepository userPointRepository;

    //상품준비
    @Autowired
    ProductRepository productRepository;

    // 유스케이스 전체 흐름 테스트 시 사용 (AfterCommit 발행 확인용)
    @Autowired OrderTransactionServiceImpl orderTxService;

    // 프로듀서 직접 호출 테스트 시 사용
    @Autowired OrderEventProducer producer;

    // 컨슈머/서비스 호출 검증
    @SpyBean(name = "orderEventConsumer")
    OrderEventConsumer consumerSpy;
    @SpyBean OrderKafkaService serviceSpy;

    // DB 검증
    @Autowired OrderKafkaRepository orderRepo;
    @Autowired OrderKafkaItemRepository itemRepo;

    private List<OrderItemCommand> itemsCmd;

    @BeforeEach
    public void setup() {

        userPointRepository.save(new UserPointJPA(777L,1_000_000_000,System.currentTimeMillis()));

        LocalDateTime now = LocalDateTime.now();

        var p1 = productRepository.save(new ProductJPA(null, "P-0", 1000L, 10, now, now));
        var p2 = productRepository.save(new ProductJPA(null, "P-1", 1001L, 10, now, now));
        var p3 = productRepository.save(new ProductJPA(null, "P-2", 1002L, 10, now, now));

        this.itemsCmd = List.of(
                new OrderItemCommand(p1.getProductId(), p1.getName(), p1.getPrice(), 1),
                new OrderItemCommand(p2.getProductId(), p2.getName(), p2.getPrice(), 2),
                new OrderItemCommand(p3.getProductId(), p3.getName(), p3.getPrice(), 3)
        );

    }
    @BeforeEach
    void moveCursorToEnd() {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cleaner-" + UUID.randomUUID()); // 항상 새로운 그룹
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (var consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(props)) {
            consumer.subscribe(List.of(TOPIC));
            consumer.poll(Duration.ofSeconds(1));                 // 할당 유도
            var assignment = consumer.assignment();
            if (!assignment.isEmpty()) {
                consumer.seekToEnd(assignment);                   // 커서를 끝으로 이동 = 과거 무시
            }
        }
    }


    // ============= 1) 트랜잭션 AFTER_COMMIT → Kafka → Consumer → DB 저장 (풀체인) =============
    @Test
    @DisplayName("트랜잭션 AFTER_COMMIT → Kafka → Consumer → DB 저장 (풀체인)")
    void transaction_afterCommit_to_kafka_and_persist_to_db() {

        // 유스케이스 실행 → 커밋 후 @TransactionalEventListener(AFTER_COMMIT)에서 Kafka 발행
        orderTxService.execute(new OrderCommand(777L, null, itemsCmd));

        // (1) @KafkaListener 호출됨(역직렬화 OK)
        ArgumentCaptor<OrderPlacedKafka> msgCap = ArgumentCaptor.forClass(OrderPlacedKafka.class);
        await().atMost(15, SECONDS).untilAsserted(() ->
                verify(consumerSpy, atLeastOnce())
                        .onMessage(msgCap.capture(), anyString(), anyInt(), anyLong())
        );
        var consumed = msgCap.getValue();
        assertNotNull(consumed);
        assertTrue(consumed.items().size() >= 3);

        // (2) 서비스 호출됨
        await().atMost(10, SECONDS).untilAsserted(() ->
                verify(serviceSpy, atLeastOnce()).save(any(OrderPlacedKafka.class))
        );

        // (3) 실제 DB 반영됨 (헤더 + 아이템 3건)
        Long persistedOrderId = consumed.orderId();
        await().atMost(15, SECONDS).untilAsserted(() -> {
            assertTrue(orderRepo.findById(persistedOrderId).isPresent());
            assertEquals(3, itemRepo.findByOrderEvent_OrderId(persistedOrderId).size());
        });

        // 내용 스팟 체크
        List<OrderKafkaItemJPA> items = itemRepo.findByOrderEvent_OrderId(persistedOrderId);
        assertTrue(items.stream().anyMatch(i -> i.getProductName().equals("P-1")));
        assertTrue(items.stream().anyMatch(i -> i.getQuantity() == 3));
    }

    // ============= 2) 토픽 원본: JSON 구조/타입 + 헤더 검증 (= "토픽만 확인"을 넘어서 스키마 보장) =============
    @Test
    @DisplayName("토픽 원본: JSON 구조/타입 + 헤더 검증 (= 토픽만 확인을 넘어서 스키마 보장)")
    void producer_emits_valid_json_and_headers() throws Exception {



        // 이 테스트 전용 고유 orderId/Key
        long uniqueOrderId = System.currentTimeMillis();
        var payload = sample(uniqueOrderId, 2);
        String targetKey = String.valueOf(uniqueOrderId);

        // Raw consumer 준비 (명시적 assign 사용)
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "raw-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // auto-commit 굳이 필요 없음(읽기 전용). 기본값 true라도 무방.

        try (var raw = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(props)) {

            // 1) 토픽 파티션 메타 조회 → 명시적 할당(assign)
            var partitionsInfo = raw.partitionsFor(TOPIC);
            assertNotNull(partitionsInfo);
            assertFalse(partitionsInfo.isEmpty(), "no partitions for topic");

            var tps = new ArrayList<TopicPartition>();
            for (var p : partitionsInfo) {
                tps.add(new TopicPartition(p.topic(), p.partition()));
            }
            raw.assign(tps);

            // 2) 현재 각 파티션의 end offset 스냅샷(= 현시점까지 쌓인 마지막 오프셋 + 1)
            var endOffsets = raw.endOffsets(tps);
            // position 갱신을 위해 짧게 poll
            raw.poll(Duration.ofMillis(200));

            // 3) 동기 전송 보장 (테스트에서만)
            var msg = org.springframework.messaging.support.MessageBuilder.withPayload(payload)
                    .setHeader(org.springframework.kafka.support.KafkaHeaders.TOPIC, TOPIC)
                    .setHeader(org.springframework.kafka.support.KafkaHeaders.KEY, targetKey)
                    .setHeader("eventType", "OrderPlaced")
                    .setHeader("schemaVersion", "1")
                    .build();
            kafkaTemplate.send(msg).get(5, java.util.concurrent.TimeUnit.SECONDS);
            kafkaTemplate.flush();

            // 4) 스냅샷 이후로 들어오는 레코드 중 (키==targetKey && orderId==uniqueOrderId) 만 찾기
            var recRef = new java.util.concurrent.atomic.AtomicReference<
                    org.apache.kafka.clients.consumer.ConsumerRecord<String, String>>();

            await()
                    .pollInterval(java.time.Duration.ofMillis(300))
                    .atMost(30, SECONDS)
                    .untilAsserted(() -> {
                        var polled = raw.poll(java.time.Duration.ofMillis(800));
                        // 비어있어도 계속 재시도하므로 여기선 단정 안 함

                        // Iterable → Stream
                        var stream = StreamSupport.stream(polled.spliterator(), false)
                                // (a) 스냅샷 이후 오프셋만 인정
                                .filter(r -> {
                                    var snapEnd = endOffsets.get(new TopicPartition(r.topic(), r.partition()));
                                    return snapEnd != null && r.offset() >= snapEnd;
                                })
                                // (b) 키 일치
                                .filter(r -> targetKey.equals(r.key()))
                                // (c) 페이로드 orderId 일치
                                .filter(r -> {
                                    try {
                                        var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(r.value());
                                        return root.hasNonNull("orderId")
                                                && root.get("orderId").isIntegralNumber()
                                                && root.get("orderId").longValue() == uniqueOrderId;
                                    } catch (Exception e) {
                                        return false;
                                    }
                                });

                        var opt = stream.findFirst();
                        org.junit.jupiter.api.Assertions.assertTrue(opt.isPresent(), "no matching record yet");
                        recRef.set(opt.get());
                    });

            var rec = recRef.get();
            org.junit.jupiter.api.Assertions.assertNotNull(rec, "record should not be null");

            // --- 헤더 검증 ---
            var eventType = rec.headers().lastHeader("eventType");
            var schemaV   = rec.headers().lastHeader("schemaVersion");
            org.junit.jupiter.api.Assertions.assertNotNull(eventType);
            org.junit.jupiter.api.Assertions.assertEquals("OrderPlaced",
                    new String(eventType.value(), java.nio.charset.StandardCharsets.UTF_8));
            org.junit.jupiter.api.Assertions.assertNotNull(schemaV);
            org.junit.jupiter.api.Assertions.assertEquals("1",
                    new String(schemaV.value(), java.nio.charset.StandardCharsets.UTF_8));

            // --- JSON 구조/타입 검증 ---
            var om = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = om.readTree(rec.value());
            org.junit.jupiter.api.Assertions.assertEquals(uniqueOrderId, root.get("orderId").longValue());
            org.junit.jupiter.api.Assertions.assertTrue(root.hasNonNull("userId") && root.get("userId").isIntegralNumber());
            org.junit.jupiter.api.Assertions.assertTrue(root.hasNonNull("totalPrice") && root.get("totalPrice").isIntegralNumber());
            org.junit.jupiter.api.Assertions.assertTrue(root.hasNonNull("payPoint") && root.get("payPoint").isIntegralNumber());
            org.junit.jupiter.api.Assertions.assertTrue(
                    root.hasNonNull("createdAt") &&
                            (root.get("createdAt").isTextual() || root.get("createdAt").isArray()),
                    "createdAt must be ISO string or array timestamp"
            );
            org.junit.jupiter.api.Assertions.assertTrue(root.has("items") && root.get("items").isArray());
            org.junit.jupiter.api.Assertions.assertEquals(2, root.get("items").size());
            var first = root.get("items").get(0);
            org.junit.jupiter.api.Assertions.assertTrue(first.hasNonNull("productId") && first.get("productId").isIntegralNumber());
            org.junit.jupiter.api.Assertions.assertTrue(first.hasNonNull("productName") && first.get("productName").isTextual());
            org.junit.jupiter.api.Assertions.assertTrue(first.hasNonNull("pricePerUnit") && first.get("pricePerUnit").isIntegralNumber());
            org.junit.jupiter.api.Assertions.assertTrue(first.hasNonNull("quantity") && first.get("quantity").isIntegralNumber());
        }
    }

    // ----------------- 헬퍼 -----------------
    private OrderPlacedKafka sample(long orderId, int itemCount) {
        var items = new ArrayList<OrderPlacedKafka.OrderItemLine>();
        for (int i = 0; i < itemCount; i++) {
            items.add(new OrderPlacedKafka.OrderItemLine(
                    10L + i, "P-" + i, 1000L + i, 1 + i
            ));
        }
        return new OrderPlacedKafka(
                orderId, 777L, 555L, 12345L, 500L, items, LocalDateTime.now()
        );
    }

}
