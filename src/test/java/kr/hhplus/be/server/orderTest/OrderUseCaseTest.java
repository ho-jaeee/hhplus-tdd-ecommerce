package kr.hhplus.be.server.orderTest;


import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.order.domain.model.Order;
import kr.hhplus.be.server.order.domain.model.OrderStatus;
import kr.hhplus.be.server.order.domain.service.OrderSaveService;
import kr.hhplus.be.server.order.domain.service.OrderItemSaveService;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.usecase.OrderUseCase;
import kr.hhplus.be.server.order.usecase.OrderCommand;
import kr.hhplus.be.server.order.usecase.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.OrderResult;
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

import java.time.LocalDateTime;
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
    @Mock OrderSaveService orderSaveService;
    @Mock OrderItemSaveService orderItemSaveService;

    @Mock OrderHistoryService orderHistoryService;
    @Mock ProductDecreaseService productDecreaseService;
    @Mock ProductHistoryService productHistoryService;

    @InjectMocks
    OrderUseCase orderUseCase;

    @Test
    @DisplayName("정상 주문이 완료되면 PAID 상태를 반환한다.")
    void productOrderTest() {
        // given
        Long userId = 1L;
        Long couponId = 10L;

        List<OrderItemCommand> itemCommands = List.of(
                new OrderItemCommand(1001L, "키보드", 50000L, 1),
                new OrderItemCommand(1002L, "마우스", 30000L, 2)
        );
        OrderCommand command = new OrderCommand(userId, couponId, itemCommands);

        long totalPrice = 50000L + 30000L * 2; // 110,000원
        int discountPercent = 10;
        long discountedPrice = 99000L;

        // stub
        given(couponDiscountService.getDiscountPercent(couponId)).willReturn(discountPercent);

        Order order = Order.builder()
                .orderId(999L)
                .userId(userId)
                .couponId(couponId)
                .totalPrice(totalPrice)
                .discountedTotalPrice(discountedPrice)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        given(orderSaveService.save(any(Order.class))).willReturn(order);

        given(orderItemSaveService.itemSave(order.getOrderId(), anyList())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OrderResult result = orderUseCase.createOrder(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(999L);
        assertThat(result.discountedPrice()).isEqualTo(discountedPrice);
        assertThat(result.status()).isEqualTo("PAID");
        assertThat(result.items()).hasSize(2);


        then(productCheckService).should(times(2)).stockCheck(anyLong(), anyInt());
        then(productDecreaseService).should(times(2)).decreaseStock(anyLong(), anyInt());
        then(couponCheckService).should().checkCoupon(userId, couponId);
        then(pointUseService).should().usePoint(userId, discountedPrice);
        then(orderSaveService).should().save(any(Order.class));
        then(orderItemSaveService).should().itemSave(order.getOrderId(),anyList());
        then(orderHistoryService).should().orderInsert(any(Order.class), eq("결제완료"));
        then(productHistoryService).should(times(2))
                .insertHistory(any(), any(), any(), anyInt(), any(), any());


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
