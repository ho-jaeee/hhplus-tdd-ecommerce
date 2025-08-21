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
public class OrderUseCasePointConcurrencyTest {

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
                LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 상품 2
        productId2 = productRepository.save(new ProductJPA(
                null, "마우스", 30000L, 10,
                LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 포인트 세팅
        userPointRepository.save(new UserPointJPA(userId, 50000L, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)));


        // 쿠폰 발급
        couponId = couponRepository.save(new CouponJPA(
                null, "10% 할인", 10, 100, 0,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now(), LocalDateTime.now()
        )).getCouponId();

        couponUserRepository.save(new CouponUserJPA(
                null, couponId, userId, null,false, null, LocalDateTime.now()
        ));
    }

    @Test
    @DisplayName("사용자가 동시에 2개의 주문을 동시에 주문하면 1개는 성공 다른1개는 포인트 부족이 나와야함")
    void OrderUseCasePointConcurrency() throws InterruptedException {

        // given
        OrderCommand orderForProduct1 = new OrderCommand(
                userId,
                couponId,
                List.of(new OrderItemCommand(productId1, "키보드", 50000L, 1))
        );

        OrderCommand orderForProduct2 = new OrderCommand(
                userId,
                null,
                List.of(new OrderItemCommand(productId2, "마우스", 30000L, 2))
        );


            int threadCount = 2;
            ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            List<String> results = Collections.synchronizedList(new ArrayList<>());
            List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        executorService.submit(() -> {
            try {
                orderUseCase.createOrder(orderForProduct1);
                results.add("success");
            } catch (Throwable e) {
                results.add("fail");
                exceptions.add(e);
                System.out.println("product1 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                orderUseCase.createOrder(orderForProduct2);
                results.add("success");
            } catch (Throwable e) {
                results.add("fail");
                exceptions.add(e);
                System.out.println("product2 예외: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });


            latch.await();
            executorService.shutdown();

            // then
            long successCount = results.stream().filter("success"::equals).count();
            long failureCount = results.stream().filter("fail"::equals).count();

            System.out.println("성공한 주문 수: " + successCount);
            System.out.println("실패한 주문 수: " + failureCount);
            assertThat(successCount).isEqualTo(1L);
            assertThat(failureCount).isEqualTo(1L);

        // 예외 메시지 검증 추가
        assertThat(exceptions)
                .hasSize(1)
                .allSatisfy(ex -> assertThat(ex.getMessage()).isEqualTo("포인트가 부족합니다"));

            // 포인트 차감 금액 = 둘 중 하나 (할인 포함 계산)
            long discountedPrice1 = 50000L * 90 / 100;           // 키보드 주문
            long discountedPrice2 = 0L;                         // 마우스 주문
            long remainingPoint = userPointRepository.findById(userId).getPoint(); // orElseThrow 안됨 주의


            // 둘 중 하나만 차감되었는지 확인
            assertThat(remainingPoint)
                .isIn(50000L - discountedPrice1, 50000L - discountedPrice2);

            // 포인트 히스토리도 1건이어야 함
            assertThat(userPointHistoryRepository.findAll()).hasSize(1);
        }

    }

