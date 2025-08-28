package kr.hhplus.be.server.product.component;


import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;

import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ProductSalesCacheAggregateHandler {

    private final ProductPopularCacheService productPopularCacheService;

    @EventListener
    public void ProductSalesCacheAggregate(ProductSalesCacheEvent  e) {
        var batch = e.batch();

        try {
            var popularDtos = batch.getItems().stream()
                    .map(i -> new ProductPopularDto(i.getProductId(), i.getProductName(), i.getQuantity()))
                    .toList();

            productPopularCacheService.addSalesBatch(
                    popularDtos,
                    batch.getOccurred(),
                    batch.getEventId()
            );
        } catch (Exception ignore) {
            // 캐시 실패는 주문 트랜잭션과 분리
        }
    }
}
