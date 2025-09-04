package kr.hhplus.be.server.order.component;

import kr.hhplus.be.server.common.event.DomainEventPublisher;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.product.component.ProductSalesCacheEvent;
import kr.hhplus.be.server.product.component.dto.ProductSalesCacheBatch;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;


import kr.hhplus.be.server.product.domain.service.ProductPopularSaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Component
@RequiredArgsConstructor
public class OrderEventHandler {


    private final OrderHistoryService orderHistoryService;
    private final ProductHistoryService productHistoryService;
    private final ProductPopularSaveService productPopularSaveService;
    private final DomainEventPublisher eventPublisher;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onOrderPlaced(OrderPlacedEvent e) {
        // 1) 주문 이력
        orderHistoryService.orderInsert(e.getOrder(), "결제완료");

        // 2) 상품 이력: 이벤트의 모든 아이템에 대해 단건 API 호출
        e.getItems().forEach(item ->
                productHistoryService.insertHistory(
                        item.productId(),
                        e.getOrderId(),
                        ProductHistoryJPA.ChangeType.SALE,
                        item.quantity(),
                        item.productName(),
                        item.pricePerUnit()
                )
        );

        // 3) 상품 판매량 집계(DB)
        e.getItems().forEach(item ->
                productPopularSaveService.addSale(
                        item.productId(),
                        item.quantity(),
                        LocalDateTime.now()
                )
        );


    }
}
