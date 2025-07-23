package kr.hhplus.be.server.orderTest;


import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.order.controller.dto.OrderItemRequest;
import kr.hhplus.be.server.order.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.usecase.OrderUseCaseImpl;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
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
    @Mock ProductHistoryService productHistoryService;

    @InjectMocks
    OrderUseCaseImpl orderUseCase;

    @Test
    @DisplayName("정상 주문이 완료되면 PAID 상태를 반환한다.")
    void productOrderTest() {
        // ────────────── Given ──────────────
        Long userId = 1L;
        Long couponId = 10L;

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1001L, "셔츠", 15000L, 2),  // 30,000
                new OrderItemRequest(1002L, "바지", 20000L, 1)   // 20,000
        );

        OrderRequest request = new OrderRequest(userId, couponId, items);

        long totalPrice = 50000L;        // 30,000 + 20,000
        int discountPercent = 10;
        long discountedPrice = 45000L;   // 10% 할인

        // 쿠폰 할인 퍼센트 반환
        given(couponDiscountService.getDiscountPercent(couponId)).willReturn(discountPercent);

        // 주문 저장 시 리턴할 mock 객체
        OrderJPA mockSavedOrder = OrderJPA.builder()
                .orderId(999L)
                .userId(userId)
                .couponId(couponId)
                .totalPrice(totalPrice)
                .discountedTotalPrice(discountedPrice)
                .status(OrderJPA.OrderStatus.CREATED)
                .build();
        given(orderRepository.save(any())).willReturn(mockSavedOrder);

        // 아이템 저장 시 그대로 반환
        given(orderItemRepository.insert(any())).willAnswer(invocation -> invocation.getArgument(0));

        // ────────────── When ──────────────
        OrderJPA result = orderUseCase.createOrder(request);

        // ────────────── Then ──────────────
        // 주문 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(999L);
        assertThat(result.getTotalPrice()).isEqualTo(totalPrice);
        assertThat(result.getDiscountedTotalPrice()).isEqualTo(discountedPrice);
        assertThat(result.getCouponId()).isEqualTo(couponId);

        // 서비스 호출 검증
        then(productCheckService).should(times(2)).stockCheck(anyLong(), anyInt());
        then(couponCheckService).should().checkCoupon(userId, couponId);
        then(pointUseService).should().usePoint(userId, discountedPrice);

        // 저장 호출 검증
        then(orderRepository).should().save(any(OrderJPA.class));
        then(orderItemRepository).should(times(2)).insert(any(OrderItemJPA.class));

        // 이력 저장 검증
        then(orderHistoryService).should().orderInsert(any(OrderJPA.class), eq("결제완료"));
        then(productHistoryService).should(times(2)).insertHistory(
                any(Long.class),    // productId
                any(Long.class),    // orderId
                any(ProductHistoryJPA.ChangeType.class),
                anyInt(),           // quantity
                any(String.class),  // productName
                any(Long.class)     // pricePerUnit
        );
    }

    @Test
    @DisplayName("상품 재고가 부족하면 예외가 발생한다")
    void shouldFail_whenStockIsInsufficient() {
        // given
        Long userId = 1L;
        Long couponId = 10L;
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1001L, "셔츠", 15000L, 100)  // 수량이 비정상적으로 많음
        );
        OrderRequest request = new OrderRequest(userId, couponId, items);

        // 재고 체크 중 예외 발생
        willThrow(new IllegalStateException("재고 부족")).given(productCheckService)
                .stockCheck(1001L, 100);

        // when & then
        assertThatThrownBy(() -> orderUseCase.createOrder(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("재고 부족");

        // 호출 여부 검증
        then(productCheckService).should().stockCheck(1001L, 100);
        then(couponCheckService).shouldHaveNoInteractions();
        then(pointUseService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("쿠폰이 유효하지 않으면 예외가 발생한다")
    void shouldFail_whenCouponIsInvalid() {
        // given
        Long userId = 1L;
        Long couponId = 99L;
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1001L, "바지", 20000L, 1)
        );
        OrderRequest request = new OrderRequest(userId, couponId, items);

        // 재고는 정상
        willDoNothing().given(productCheckService).stockCheck(anyLong(), anyInt());

        // 쿠폰 유효성 검사 중 예외 발생
        willThrow(new IllegalArgumentException("유효하지 않은 쿠폰")).given(couponCheckService)
                .checkCoupon(userId, couponId);

        // when & then
        assertThatThrownBy(() -> orderUseCase.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 쿠폰");

        // 호출 여부 검증
        then(productCheckService).should().stockCheck(1001L, 1);
        then(couponCheckService).should().checkCoupon(userId, couponId);
        then(pointUseService).shouldHaveNoInteractions();
    }

}
