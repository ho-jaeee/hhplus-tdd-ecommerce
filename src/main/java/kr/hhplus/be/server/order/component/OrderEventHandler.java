package kr.hhplus.be.server.order.component;

import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;



@Component
@RequiredArgsConstructor
public class OrderEventHandler {


    private final OrderHistoryService orderHistoryService;
    private final ProductHistoryService productHistoryService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Order(1)
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

    }
}
