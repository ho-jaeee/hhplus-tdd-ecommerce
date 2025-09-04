package kr.hhplus.be.server.product.component.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
@Getter
public class ProductSalesCacheBatch {

    List<Item> items;
    Instant occurred;     // KST→UTC 변환된 Instant
    String eventId;       // 없으면 "order:{orderId}"

    @Value
    @Builder
    public static class Item {
        Long productId;
        String productName;
        Long quantity;
    }
}
