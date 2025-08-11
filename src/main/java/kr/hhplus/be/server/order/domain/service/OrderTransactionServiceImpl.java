package kr.hhplus.be.server.order.domain.service;


import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.lockCoordinator.LockCoordinator;
import kr.hhplus.be.server.common.DomainEventPublisher;
import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.service.dto.OrderItem;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.pollicy.OrderPriceCalculator;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import kr.hhplus.be.server.product.domain.service.ProductDecreaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTransactionServiceImpl implements OrderTransactionService {

    private final LockCoordinator lockCoordinator;
    private final CouponDiscountService couponDiscountService; // 비관적 락
    private final PointUseService pointUseService; // 비관적 락
    private final OrderSaveService orderSaveService;
    private final OrderItemSaveService orderItemSaveService;
    private final ProductDecreaseService productDecreaseService;// 낙관적 락
    private final DomainEventPublisher eventPublisher;


    @Transactional
    public OrderResult execute(OrderCommand command){
        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();

        // 0) 락 순서 표준화 강제 (Coupon → UserPoint → Product asc)
        lockCoordinator.lockInOrder(couponId, userId,
                items.stream().map(OrderItemCommand::productId).sorted().toList());


        // 1) 가격 계산 & 쿠폰 사용 확정(used 처리 + 할인률 반환)
        long totalPrice = OrderPriceCalculator.calculateTotalPrice(items);
        int discountPercent = (couponId != null)
                ? couponDiscountService.useCoupon(userId, couponId) : 0;
        long payPoint = totalPrice * (100 - discountPercent) / 100;

        // 2) 포인트 차감
        pointUseService.usePoint(userId, payPoint);

        // 3) 재고 차감 (낙관적 락 + 내부 재시도 권장)
        productDecreaseService.decreaseStocksWithRetry(items); // << 다건 처리 + 재시도

        // 4) 주문/아이템 저장 (한 번에)
        Order savedOrder = orderSaveService.save(Order.create(
                userId, couponId, totalPrice, payPoint, OrderStatus.PAID, now(), now()));

        orderItemSaveService.itemSave(savedOrder.getOrderId(),
                items.stream().map(i -> OrderItem.create(
                        savedOrder.getOrderId(),
                        i.productId(),
                        i.productName(),
                        i.pricePerUnit(),
                        i.quantity()))
                        .toList());


        // 5) 도메인 이벤트 발행 (AFTER_COMMIT 핸들러가 후처리)
        eventPublisher.publish(OrderPlacedEvent.of(items, savedOrder, payPoint));

        // 6) 결과
        return OrderResult.of(savedOrder, items);


    }
    private LocalDateTime now() { return LocalDateTime.now(); }
}
