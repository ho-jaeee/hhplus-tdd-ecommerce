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
public class OrderUseCaseCouponConcurrencyTest {

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
    OrderRepository orderRepository;

    Long userId;
    Long productId1;
    Long productId2;
    Long couponId;

    @BeforeEach
    void setUp() {
        userId = 1L;

        // 상품 1
        productId1 = productRepository.save(new ProductJPA(
                null, "키보드", 50000L, 10,
                null, LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 상품 2
        productId2 = productRepository.save(new ProductJPA(
                null, "마우스", 30000L, 10,
                null, LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 포인트 세팅
        userPointRepository.save(new UserPointJPA(userId, 1000000L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));

        // 쿠폰 발급
        couponId = couponRepository.save(new CouponJPA(
                null, "10% 할인", 10, 100, 0,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        )).getCouponId();

        couponUserRepository.save(new CouponUserJPA(
                null, couponId, userId, false, null, LocalDateTime.now()
        ));
    }

    @Test
    @DisplayName("사용자가 하나의 쿠폰으로 동시에 2개의 주문을 시도하면 하나만 성공한다")
    void OrderUseCaseCouponConcurrency() throws InterruptedException{
        // given
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(productId1, "키보드", 50000L, 1),
                new OrderItemCommand(productId2, "마우스", 30000L, 2)
        );
        OrderCommand command = new OrderCommand(userId, couponId, items);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<String> results = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderUseCase.createOrder(command);
                    results.add("success");
                } catch (Throwable e) {
                    results.add("fail");
                    exceptions.add(e);
                   //e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기
        executorService.shutdown();

        // then
        long successCount = results.stream().filter("success"::equals).count();
        long failureCount = results.stream().filter("fail"::equals).count();

        System.out.println("성공한 스레드 수: " + successCount);
        System.out.println("실패한 스레드 수: " + failureCount);

        assertThat(successCount).isEqualTo(1L);
        assertThat(failureCount).isEqualTo(1L);
    }


}
