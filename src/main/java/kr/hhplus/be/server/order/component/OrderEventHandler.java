package kr.hhplus.be.server.order.component;

import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.ProductPopularSaveService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class OrderEventHandler {


    private final OrderHistoryService orderHistoryService;
    private final ProductHistoryService productHistoryService;
    private final ProductPopularSaveService productPopularSaveService;
    private final ProductPopularCacheService productPopularCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent e) {
        // 1) 주문 이력
        orderHistoryService.orderInsert(e.getOrder(), "결제완료");

        // 2) 상품 이력
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
                        LocalDateTime.from(e.getCreatedAt())
                )
        );

        // 4) 상품 판매량 집계(레디스 캐시)
        try {
            List<ProductPopularDto> popularDtos = e.getItems().stream()
                    .map(it -> new ProductPopularDto(it.productId(), it.productName(), it.quantity()))
                    .toList();

            String eventId = (e.getEventId() != null && !e.getEventId().isBlank())
                    ? e.getEventId()
                    : "order:" + e.getOrderId();

            productPopularCacheService.addSalesBatch(popularDtos, e.getCreatedAt(), eventId);
        } catch (Exception ex) {
            // log.warn("popular cache update failed. orderId={}, cause={}", e.getOrderId(), ex.toString(), ex);
        }
    }
}
