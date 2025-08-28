package kr.hhplus.be.server.integrationTest.concurrencyTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.config.RedissonTestConfig;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.order.usecase.OrderUseCase;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class OrderUseCaseMultiLockTest {

    @Autowired
    OrderUseCase orderUseCase;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserPointRepository userPointRepository;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    CouponUserRepository  couponUserRepository;


    List<Long> userIds;
    Long couponId;
    Long productId;
    String productName;
    Long pricePerUnit;

    @BeforeEach
    void setUp() {

        CouponJPA c = new CouponJPA(
                null,
                "10%할인 쿠폰",
                10,
                100,
                50,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        );
        couponRepository.save(c);
        couponId = c.getCouponId();

        userIds = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            UserPointJPA u = new UserPointJPA(
                    1000L + i, // 서로 다른 userId
                    10000000,
                    System.currentTimeMillis()
            );
            CouponUserJPA cu = new CouponUserJPA(
                    null,
                    1000L + i,
                    c.getCouponId(),
                    null,
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now()
                    );
            userPointRepository.save(u);
            userIds.add(u.getUserId());
            couponUserRepository.save(cu);
        }

        ProductJPA p = new ProductJPA(
                null,
                "동시성-테스트",
                1000L,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        productRepository.save(p);
        productId = p.getProductId();
        productName = p.getName();
        pricePerUnit = p.getPrice();

    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        userPointRepository.deleteAll();
        couponRepository.deleteAll();
        couponUserRepository.deleteAll();

    }


    @Test
    @DisplayName("동시 20요청 -> 10 성공 / 10 재고부족 (락 타임아웃 0)")
    void concurrentOrder() throws Exception {
        final int n = 20;
        ExecutorService pool = Executors.newFixedThreadPool(n);

        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(n);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();
        AtomicInteger lockTimeout = new AtomicInteger();
        AtomicInteger other = new AtomicInteger();

        for (int i = 0; i < n; i++) {
            final Long userId = userIds.get(i);
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await(10, TimeUnit.SECONDS); // 동시에 출발
                    OrderCommand cmd = new OrderCommand(
                            userId,
                            couponId,
                            List.of(new OrderItemCommand(productId,productName, pricePerUnit, 1))
                    );
                    orderUseCase.createOrder(cmd);
                    success.incrementAndGet();
                } catch (Exception e) {
                    String msg = String.valueOf(e.getMessage());
                    System.out.println("[FAIL] " + e.getClass().getSimpleName() + " : " + msg);
                    System.out.println("isAopProxy=" + AopUtils.isAopProxy(orderUseCase));
                    if (msg.contains("잠시 후 다시 시도"))        lockTimeout.incrementAndGet(); // tryLock 대기만료
                    else if (msg.contains("재고") || msg.contains("out of stock")) outOfStock.incrementAndGet();
                    else                                         other.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        // 모든 작업자 대기 → 동시에 시작
        ready.await(10, TimeUnit.SECONDS);
        start.countDown();

        // 종료 대기
        done.await(60, TimeUnit.SECONDS);
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(success.get()).isEqualTo(10);
        assertThat(outOfStock.get()).isEqualTo(10);
        assertThat(lockTimeout.get()).isEqualTo(0);
        assertThat(other.get()).isEqualTo(0);

        assertThat(productRepository.findById(productId).orElseThrow().getQuantity()).isZero();
    }
}
