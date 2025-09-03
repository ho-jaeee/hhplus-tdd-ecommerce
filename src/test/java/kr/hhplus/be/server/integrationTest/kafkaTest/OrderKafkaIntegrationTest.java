package kr.hhplus.be.server.integrationTest.kafkaTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
        var payload = sample(20002L, 2);
        producer.send(payload);

        // Raw consumer로 스키마/헤더 단정
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "raw-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        try (var raw = new org.apache.kafka.clients.consumer.KafkaConsumer<String, String>(props)) {
            raw.subscribe(Collections.singletonList(TOPIC));

            var holder = new Object(){ org.apache.kafka.clients.consumer.ConsumerRecord<String,String> rec; };
            await().atMost(10, SECONDS).untilAsserted(() -> {
                var polled = raw.poll(Duration.ofMillis(500));
                assertFalse(polled.isEmpty(), "no records yet");
                holder.rec = polled.iterator().next();
            });

            var rec = holder.rec;

            // 헤더
            var eventType = rec.headers().lastHeader("eventType");
            var schemaV   = rec.headers().lastHeader("schemaVersion");
            assertNotNull(eventType); assertEquals("OrderPlaced", new String(eventType.value()));
            assertNotNull(schemaV);   assertEquals("1", new String(schemaV.value()));

            // JSON 구조/타입
            var om = new ObjectMapper();
            JsonNode root = om.readTree(rec.value());
            assertTrue(root.hasNonNull("orderId") && root.get("orderId").isIntegralNumber());
            assertTrue(root.hasNonNull("userId") && root.get("userId").isIntegralNumber());
            assertTrue(root.hasNonNull("totalPrice") && root.get("totalPrice").isIntegralNumber());
            assertTrue(root.hasNonNull("payPoint") && root.get("payPoint").isIntegralNumber());
            assertTrue(root.hasNonNull("createdAt") &&
                            (root.get("createdAt").isTextual() || root.get("createdAt").isArray()),
                    "createdAt must be ISO string or array timestamp");
            assertTrue(root.has("items") && root.get("items").isArray());
            assertEquals(2, root.get("items").size());
            JsonNode first = root.get("items").get(0);
            assertTrue(first.hasNonNull("productId") && first.get("productId").isIntegralNumber());
            assertTrue(first.hasNonNull("productName") && first.get("productName").isTextual());
            assertTrue(first.hasNonNull("pricePerUnit") && first.get("pricePerUnit").isIntegralNumber());
            assertTrue(first.hasNonNull("quantity") && first.get("quantity").isIntegralNumber());
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
