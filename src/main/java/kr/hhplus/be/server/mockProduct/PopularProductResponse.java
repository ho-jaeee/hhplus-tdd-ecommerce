package kr.hhplus.be.server;

public record PopularProductResponse(
        Long productId,
        String name,
        Long price,
        int soldCount//3일간 판매된 수량 집계 Mock API용
)
{}
