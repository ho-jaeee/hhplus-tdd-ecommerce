package kr.hhplus.be.server.orderTest;


import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.usecase.OrderUseCaseImpl;
import kr.hhplus.be.server.order.domain.model.OrderCommand;
import kr.hhplus.be.server.order.domain.model.OrderItemCommand;
import kr.hhplus.be.server.order.domain.model.OrderResult;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import kr.hhplus.be.server.product.domain.service.ProductDecreaseService;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class OrderUseCaseTest {

    @Mock ProductCheckService productCheckService;
    @Mock CouponCheckService couponCheckService;
    @Mock CouponDiscountService couponDiscountService;
    @Mock PointUseService pointUseService;
    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock OrderHistoryService orderHistoryService;
    @Mock ProductDecreaseService productDecreaseService;
    @Mock ProductHistoryService productHistoryService;

    @InjectMocks
    OrderUseCaseImpl orderUseCase;

    @Test
    @DisplayName("정상 주문이 완료되면 PAID 상태를 반환한다.")
    void productOrderTest() {
        // Given
        Long userId = 1L;
        Long couponId = 10L;

        List<OrderItemCommand> items = List.of(
                new OrderItemCommand(1001L, "셔츠", 15000L, 2),
                new OrderItemCommand(1002L, "바지", 20000L, 1)
        );
        OrderCommand command = new OrderCommand(userId, couponId, items);

        long totalPrice = 50000L;
        int discountPercent = 10;
        long discountedPrice = 45000L;

        given(couponDiscountService.getDiscountPercent(couponId)).willReturn(discountPercent);

        given(orderRepository.save(any())).willReturn(OrderJPA.builder()
                .orderId(999L)
                .userId(userId)
                .couponId(couponId)
                .totalPrice(totalPrice)
                .discountedTotalPrice(discountedPrice)
                .status(OrderJPA.OrderStatus.PAID)
                .build());

        given(orderItemRepository.insert(any())).willAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResult result = orderUseCase.createOrder(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(999L);
        assertThat(result.discountedPrice()).isEqualTo(discountedPrice);
        assertThat(result.items()).hasSize(2);
        assertThat(result.status()).isEqualTo("PAID");

        then(productCheckService).should(times(2)).stockCheck(anyLong(), anyInt());
        then(productDecreaseService).should(times(2)).decreaseStock(anyLong(), anyInt());
        then(couponCheckService).should().checkCoupon(userId, couponId);
        then(pointUseService).should().usePoint(userId, discountedPrice);
        then(orderRepository).should().save(any(OrderJPA.class));
        then(orderItemRepository).should(times(2)).insert(any(OrderItemJPA.class));
        then(orderHistoryService).should().orderInsert(any(OrderJPA.class), eq("결제완료"));
        then(productHistoryService).should(times(2)).insertHistory(
                any(), any(), any(), anyInt(), any(), any()
        );

    }

    @Test
    @DisplayName("상품 재고가 부족하면 예외가 발생한다")
    void shouldFail_whenStockIsInsufficient() {
        // Given
        OrderCommand command = new OrderCommand(
                1L, 10L,
                List.of(new OrderItemCommand(1001L, "셔츠", 15000L, 100))
        );

        willThrow(new IllegalStateException("재고 부족")).given(productCheckService)
                .stockCheck(1001L, 100);

        // When & Then
        assertThatThrownBy(() -> orderUseCase.createOrder(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고 부족");

        then(productCheckService).should().stockCheck(1001L, 100);
        then(couponCheckService).shouldHaveNoInteractions();
        then(pointUseService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("쿠폰이 유효하지 않으면 예외가 발생한다")
    void shouldFail_whenCouponIsInvalid() {
        // Given
        OrderCommand command = new OrderCommand(
                1L, 99L,
                List.of(new OrderItemCommand(1001L, "바지", 20000L, 1))
        );

        willDoNothing().given(productCheckService).stockCheck(anyLong(), anyInt());

        willThrow(new IllegalArgumentException("유효하지 않은 쿠폰")).given(couponCheckService)
                .checkCoupon(1L, 99L);

        // When & Then
        assertThatThrownBy(() -> orderUseCase.createOrder(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 쿠폰");

        then(productCheckService).should().stockCheck(1001L, 1);
        then(couponCheckService).should().checkCoupon(1L, 99L);
        then(pointUseService).shouldHaveNoInteractions();
    }

}
