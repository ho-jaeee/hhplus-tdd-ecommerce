package kr.hhplus.be.server.product.component;


import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.ProductPopularSaveService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductSalesAggregateHandler {

    private final ProductPopularSaveService productPopularSaveService;
    private final ProductPopularCacheService productPopularCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Order(2)
    public void ProductSalesAggregate(OrderPlacedEvent e) {
        // 공통 컨텍스트
        final LocalDateTime occurredAt = LocalDateTime.from(e.getCreatedAt());
        final String eventId = (e.getEventId() != null && !e.getEventId().isBlank())
                ? e.getEventId()
                : "order:" + e.getOrderId();

        // 1) 상품 판매량 집계(DB)
        e.getItems().forEach(item ->
                productPopularSaveService.addSale(
                        item.productId(),
                        item.quantity(),
                        occurredAt
                )
        );

        // 2) 상품 판매량 집계(레디스 캐시)
        try {
            List<ProductPopularDto> popularDtos = e.getItems().stream()
                    .map(it -> new ProductPopularDto(it.productId(), it.productName(), it.quantity()))
                    .toList();

            productPopularCacheService.addSalesBatch(popularDtos, Instant.from(occurredAt), eventId);
        } catch (Exception ex) {
            // 캐시 집계 실패는 주문 트랜잭션과 분리(격리)
            // log.warn("popular cache update failed. eventId={}, cause={}", eventId, ex.toString(), ex);
        }
    }
}
