package kr.hhplus.be.server.integrationTest.concurrencyTest;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.order.usecase.OrderRedisUseCase;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

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
public class OrderRedisUseCaseProductConcurrencyTest {

    @Autowired
    OrderRedisUseCase orderRedisUseCase;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    CouponUserRepository couponUserRepository;
    @Autowired UserPointRepository userPointRepository;

    Long userId1, userId2, productId1, productId2, couponId;

    @BeforeEach
    void setUp() {
        userId1 = 1L;
        userId2 = 2L;

        productId1 = productRepository.save(new ProductJPA(
                null, "키보드", 50000L, 1, LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        productId2 = productRepository.save(new ProductJPA(
                null, "마우스", 30000L, 1, LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        userPointRepository.save(new UserPointJPA(userId1, 100000L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));
        userPointRepository.save(new UserPointJPA(userId2, 100000L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        couponId = couponRepository.save(new CouponJPA(
                null, "10% 할인", 10, 100, 0,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        )).getCouponId();

        couponUserRepository.save(new CouponUserJPA(null, couponId, userId1, null,false, null, LocalDateTime.now()));
        couponUserRepository.save(new CouponUserJPA(null, couponId, userId2, null, false, null, LocalDateTime.now()));
    }

    @Test
    @DisplayName("재고 1개 상품을 2명이 '동시에' 주문하면 1성공/1실패가 된다")
    void OrderUseCaseProductConcurrency() throws Exception {
        OrderCommand order1 = new OrderCommand(
                userId1, couponId, List.of(new OrderItemCommand(productId1, "키보드", 50000L, 1))
        );
        OrderCommand order2 = new OrderCommand(
                userId2, couponId, List.of(new OrderItemCommand(productId1, "키보드", 50000L, 1))
        );

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate  = new CountDownLatch(threadCount);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        executorService.submit(() -> {
            try {
                startGate.await();
                orderRedisUseCase.createOrder(order1);
                results.add("user1-success");
            } catch (Throwable e) {
                results.add("user1-fail");
                System.out.println("user1 예외: " + e.getMessage());
            } finally {
                doneGate.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                startGate.await();
                orderRedisUseCase.createOrder(order2);
                results.add("user2-success");
            } catch (Throwable e) {
                results.add("user2-fail");
                System.out.println("user2 예외: " + e.getMessage());
            } finally {
                doneGate.countDown();
            }
        });

        startGate.countDown();
        if (!doneGate.await(5, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new AssertionError("작업 타임아웃");
        }
        executorService.shutdown();

        long successCount = results.stream().filter(r -> r.contains("success")).count();
        long failCount    = results.stream().filter(r -> r.contains("fail")).count();

        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(1);

        ProductJPA product = productRepository.findById(productId1).orElseThrow();
        assertThat(product.getQuantity()).isEqualTo(0);

        System.out.println("결과: " + results);
    }
}