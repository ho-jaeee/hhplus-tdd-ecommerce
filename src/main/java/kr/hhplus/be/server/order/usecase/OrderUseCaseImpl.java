package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.order.domain.model.*;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.domain.service.OrderItemSaveService;
import kr.hhplus.be.server.order.domain.service.OrderSaveService;
import kr.hhplus.be.server.order.pollicy.OrderPriceCalculator;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import kr.hhplus.be.server.product.domain.service.ProductDecreaseService;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final ProductCheckService productCheckService;
    private final CouponCheckService couponCheckService;
    private final CouponDiscountService couponDiscountService;
    private final PointUseService pointUseService;
    private final OrderSaveService orderSaveService;
    private final OrderItemSaveService orderItemSaveService;
    private final OrderHistoryService orderHistoryService;
    private final ProductDecreaseService productDecreaseService;
    private final ProductHistoryService productHistoryService;

    @Override
    public OrderResult createOrder(OrderCommand command) {
        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();

        // 1. 재고 확인 + 차감
        for (OrderItemCommand item : items) {
            productCheckService.stockCheck(item.productId(), item.quantity());
        }

        // 2. 쿠폰 유효성 검사 (nullable)
        if (couponId != null) {
            couponCheckService.checkCoupon(userId, couponId);
        }

        // 3. 총 금액 계산 + 쿠폰 할인 적용
        long totalPrice = OrderPriceCalculator.calculateTotalPrice(items);
        int discountPercent = (couponId != null)
                ? couponDiscountService.getDiscountPercent(couponId)
                : 0;
        long discountedPrice = totalPrice * (100 - discountPercent) / 100;

        // 4. 포인트 차감
        pointUseService.usePoint(userId, discountedPrice);

        // 5. 주문 생성 및 저장
        Order order = Order.create(userId, couponId, totalPrice, discountedPrice, OrderStatus.PAID,
                LocalDateTime.now(), LocalDateTime.now());
        Order savedOrder = orderSaveService.save(order);


        // 6. 주문 아이템 저장
        List<OrderItem> orderItems = items.stream()
                .map(i -> OrderItem.create(i.productId(), i.productName(), i.quantity(), i.pricePerUnit()))
                .map(item -> item.withOrderId(savedOrder.getOrderId()))
                .toList();
        orderItemSaveService.itemSave(orderItems);

        // 7. 주문 이력 저장
        orderHistoryService.orderInsert(savedOrder, "결제완료");

        // 8. 재고차감 -> 일부러 나중에 함 결제 완료 후
        savedItems.forEach(item ->
                productDecreaseService.decreaseStock(
                        item.getProductId(),
                        item.getQuantity()
                )
        );

        // 9. 상품 이력 저장
        savedItems.forEach(item ->
                productHistoryService.insertHistory(
                        item.getProductId(),
                        item.getOrderId(),
                        ProductHistoryJPA.ChangeType.SALE,
                        item.getQuantity(),
                        item.getProductName(),
                        item.getPricePerUnit()
                )
        );

        // 9. UseCase 응답 객체로 변환
        return new OrderResult(
                savedOrder.getOrderId(),
                savedOrder.getUserId(),
                savedOrder.getTotalPrice(),
                savedOrder.getDiscountedTotalPrice(),
                savedItems.stream()
                        .map(i -> new OrderItemResult(
                                i.getProductId(),
                                i.getProductName(),
                                i.getQuantity(),
                                i.getTotalPrice()
                        ))
                        .toList(),
                savedOrder.getStatus().name()
        );
    }

}

