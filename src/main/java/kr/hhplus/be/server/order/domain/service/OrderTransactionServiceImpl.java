package kr.hhplus.be.server.order.domain.service;



import kr.hhplus.be.server.coupon.domain.service.CouponDiscountService;
import kr.hhplus.be.server.common.event.DomainEventPublisher;
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
import kr.hhplus.be.server.product.domain.service.ProductPopularOutboxSaveService;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularOutboxDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderTransactionServiceImpl implements OrderTransactionService {


    private final CouponDiscountService couponDiscountService;
    private final PointUseService pointUseService;
    private final OrderSaveService orderSaveService;
    private final OrderItemSaveService orderItemSaveService;
    private final ProductDecreaseService productDecreaseService;
    private final DomainEventPublisher eventPublisher;
    private final ProductPopularOutboxSaveService productPopularOutboxSaveService;



    @Transactional
    public OrderResult execute(OrderCommand command){
        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();


        // 1) 가격 계산 & 쿠폰 사용 확정(used 처리 + 할인률 반환)
        long totalPrice = OrderPriceCalculator.calculateTotalPrice(items);
        int discountPercent = (couponId != null)
                ? couponDiscountService.useCoupon(userId, couponId)
                : 0;
        long payPoint = totalPrice * (100 - discountPercent) / 100;

        // 2) 포인트 차감
        pointUseService.usePoint(userId, payPoint);

        // 3) 재고 차감
        productDecreaseService.decreaseStocks(items);

        // 4) 주문/아이템 저장
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


        // 5) 인기상품 Outbox 적재
        productPopularOutboxSaveService.writeForOrder(
                savedOrder.getCreatedAt(),
                items.stream()
                        .map(i -> new ProductPopularOutboxDto(i.productId(), i.quantity()))
                        .toList()
        );


        // 6) 도메인 이벤트 발행 (AFTER_COMMIT 핸들러가 후처리)
        eventPublisher.publish(OrderPlacedEvent.of(items, savedOrder, payPoint));


        // 7) 결과
        return OrderResult.of(savedOrder, items);


    }
    private LocalDateTime now() { return LocalDateTime.now(); }
}
