package kr.hhplus.be.server.product.domain.service.dto;

public record ProductPopularOutboxDto (
        long productId,
        int quantity
){}
