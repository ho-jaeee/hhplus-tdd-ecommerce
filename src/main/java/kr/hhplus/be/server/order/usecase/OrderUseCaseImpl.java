package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.order.controller.dto.OrderItemRequest;
import kr.hhplus.be.server.order.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final ProductCheckService productCheckService;
    private final CouponCheckService couponCheckService;
    private final CouponDiscountService couponDiscountService;
    private final PointUseService pointUseService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderHistoryService orderHistoryService;
    private final ProductHistoryService productHistoryService;

    @Override
    public OrderJPA createOrder(OrderRequest request) {
        Long userId = request.userId();
        Long couponId = request.couponId();
        List<OrderItemRequest> items = request.items();

        // 1. 재고 확인
        for (OrderItemRequest item : items) {
            productCheckService.stockCheck(item.productId(), item.quantity());
        }

        // 2. 쿠폰 유효성 검사 (nullable)
        if (couponId != null) {
            couponCheckService.checkCoupon(userId, couponId);
        }

        // 3. 총 금액 계산 + 쿠폰 할인 적용
        long totalPrice = calculateTotalPrice(items);
        int discountPercent = (couponId != null)
                ? couponDiscountService.getDiscountPercent(couponId)
                : 0;
        long discountedPrice = totalPrice * (100 - discountPercent) / 100;

        // 4. 포인트 차감
        pointUseService.usePoint(userId, discountedPrice);

        // 5. 주문 저장
        OrderJPA order = OrderJPA.builder()
                .userId(userId)
                .couponId(couponId)
                .totalPrice(totalPrice)
                .discountedTotalPrice(discountedPrice)
                .status(OrderJPA.OrderStatus.PAID)
                .build();
        OrderJPA savedOrder = orderRepository.save(order);

        // 6. 주문 아이템 저장
        List<OrderItemJPA> orderItems = items.stream()
                .map(item -> OrderItemJPA.builder()
                        .orderId(savedOrder.getOrderId())
                        .productId(item.productId())
                        .productName(item.productName())
                        .pricePerUnit(item.pricePerUnit())
                        .quantity(item.quantity())
                        .totalPrice(item.pricePerUnit() * item.quantity())
                        .build())
                .map(orderItemRepository::insert)
                .toList();

        // 7. 주문 이력 저장
        orderHistoryService.orderInsert(savedOrder, "결제완료");

        // 8. 상품 이력 저장
        orderItems.forEach(item ->
                productHistoryService.insertHistory(
                        item.getProductId(),
                        item.getOrderId(),
                        ProductHistoryJPA.ChangeType.SALE,
                        item.getQuantity(),
                        item.getProductName(),
                        item.getPricePerUnit()
                )
        );

        return savedOrder;
    }

    private long calculateTotalPrice(List<OrderItemRequest> items) {
        return items.stream()
                .mapToLong(item -> item.pricePerUnit() * item.quantity())
                .sum();
    }
}



