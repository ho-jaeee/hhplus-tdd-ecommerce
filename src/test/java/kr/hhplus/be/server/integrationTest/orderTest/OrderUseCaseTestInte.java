package kr.hhplus.be.server.integrationTest.orderTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import kr.hhplus.be.server.order.usecase.OrderUseCase;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class OrderUseCaseTestInte {

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
                LocalDateTime.now(), LocalDateTime.now()
        )).getProductId();

        // 상품 2
        productId2 = productRepository.save(new ProductJPA(
                null, "마우스", 30000L, 10,
                LocalDateTime.now(), LocalDateTime.now()
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
    @DisplayName("통합 주문 흐름이 정상적으로 처리된다")
    void order_flow_success() {
        // given
        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(productId1, "키보드", 50000L, 1),
                new OrderItemCommand(productId2, "마우스", 30000L, 2)
        );

        OrderCommand command = new OrderCommand(userId, couponId, items);

        // when
        OrderResult result = orderUseCase.createOrder(command);


        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isNotNull();
        assertThat(result.status()).isEqualTo("PAID");
        assertThat(result.items()).hasSize(2);
        assertThat(result.discountedPrice()).isEqualTo(99000L);  // 110,000 * 0.9

        // DB에 실제 저장되었는지 확인
        OrderJPA saved = orderRepository.findById(result.orderId()).orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PAID);
    }

}
