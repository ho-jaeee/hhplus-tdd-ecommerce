package kr.hhplus.be.server.integrationTest.concurrencyTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.usecase.OrderUseCase;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestcontainersConfiguration.class})
@org.testcontainers.junit.jupiter.Testcontainers
public class OrderUseCaseProductConcurrencyTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redissonProps(DynamicPropertyRegistry reg) {
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);
        reg.add("redisson.config", () -> """
      singleServerConfig:
        address: "redis://%s:%d"
      lockWatchdogTimeout: 30000
      """.formatted(host, port));
    }

    @Autowired
    OrderUseCase orderUseCase;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponUserRepository couponUserRepository;

    @Autowired
    UserPointRepository userPointRepository;

    @Autowired
    UserPointHistoryRepository userPointHistoryRepository;

    @Autowired
    OrderRepository orderRepository;

    Long userId1;
    Long userId2;
    Long productId1;
    Long productId2;
    Long couponId;


    @BeforeEach
    @Transactional
    void setUp() {
        userId1 = 1L;
        userId2 = 2L;

        // 상품 1
        productId1 = productRepository.save(new ProductJPA(
                null, "키보드", 50000L, 1,
                null, LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 상품 2
        productId2 = productRepository.save(new ProductJPA(
                null, "마우스", 30000L, 1,
                null, LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 포인트 세팅
        userPointRepository.save(new UserPointJPA(userId1, 100000L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
        userPointRepository.save(new UserPointJPA(userId2, 100000L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
        // 쿠폰 발급
        couponId = couponRepository.save(new CouponJPA(
                null, "10% 할인", 10, 100, 0,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        )).getCouponId();

        couponUserRepository.save(new CouponUserJPA(
                null, couponId, userId1, false, null, LocalDateTime.now()
        ));

        couponUserRepository.save(new CouponUserJPA(
                null, couponId, userId2, false, null, LocalDateTime.now()
        ));
    }

    @Test
    @DisplayName("재고가 1개 남은 제품을 2명의 사람이 동시에 주문한다.")
    void OrderUseCaseProductConcurrency() throws InterruptedException {

        // given
        OrderCommand order1 = new OrderCommand(
                userId1, couponId,
                List.of(new OrderItemCommand(productId1, "키보드", 50000L, 1))
        );

        OrderCommand order2 = new OrderCommand(
                userId2, couponId,
                List.of(new OrderItemCommand(productId1, "키보드", 50000L, 1))
        );


        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        executorService.submit(() -> {
            try {
                orderUseCase.createOrder(order1);
                results.add("user1-success");
            } catch (Throwable e) {
                results.add("user1-fail");
                System.out.println("user1 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                orderUseCase.createOrder(order2);
                results.add("user2-success");
            } catch (Throwable e) {
                results.add("user2-fail");
                System.out.println("user2 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        // then
        long successCount = results.stream().filter(r -> r.contains("success")).count();
        long failCount = results.stream().filter(r -> r.contains("fail")).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(1);

        ProductJPA product = productRepository.findById(productId1).orElseThrow();
        assertThat(product.getQuantity()).isEqualTo(0);

        System.out.println("결과: " + results);
    }
}
