package kr.hhplus.be.server.order.domain.service;


import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.service.dto.OrderItem;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.pollicy.OrderPriceCalculator;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemResult;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductDecreaseService;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTransactionService {

    private final CouponDiscountService couponDiscountService;
    private final PointUseService pointUseService;
    private final OrderSaveService orderSaveService;
    private final OrderItemSaveService orderItemSaveService;
    private final OrderHistoryService orderHistoryService;
    private final ProductDecreaseService productDecreaseService;
    private final ProductHistoryService productHistoryService;

    @Transactional
    public OrderResult execute(OrderCommand command) {
        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();

        // 총 금액 계산 + 쿠폰 할인 적용 + 쿠폰사용 시 used로 변경(트랜잭션 비관적 락)
        long totalPrice = OrderPriceCalculator.calculateTotalPrice(items);
        int discountPercent = (couponId != null) ?
                couponDiscountService.useCoupon(userId, couponId) : 0;
        long discountedPrice = totalPrice * (100 - discountPercent) / 100;

        //포인트사용
        pointUseService.usePoint(userId, discountedPrice);

        //주문저장
        Order order = Order.create(userId, couponId, totalPrice, discountedPrice, OrderStatus.PAID,
                LocalDateTime.now(), LocalDateTime.now());
        Order savedOrder = orderSaveService.save(order);

        //주문 아이템 저장
        List<OrderItem> orderItems = items.stream()
                .map(i -> OrderItem.create(
                        savedOrder.getOrderId(),
                        i.productId(),
                        i.productName(),
                        i.pricePerUnit(),
                        i.quantity()
                ))
                .toList();
        orderItemSaveService.itemSave(savedOrder.getOrderId(), orderItems);

        //재고차감
        orderItems.forEach(item ->
                productDecreaseService.decreaseStock(
                        item.getProductId(),
                        item.getQuantity()
                )
        );

        //주문이력 저장
        orderHistoryService.orderInsert(savedOrder, "결제완료");

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

        //상품 이력 저장
        return new OrderResult(
                savedOrder.getOrderId(),
                savedOrder.getUserId(),
                savedOrder.getTotalPrice(),
                savedOrder.getDiscountedTotalPrice(),
                orderItems.stream()
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
