package kr.hhplus.be.server.product.controller.dto;

import kr.hhplus.be.server.product.domain.model.ProductJPA;

public record ProductResponse(
        Long productId,
        String name,
        Long price
) {
    public static ProductResponse from(ProductJPA product) {
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getPrice()
        );
    }
}