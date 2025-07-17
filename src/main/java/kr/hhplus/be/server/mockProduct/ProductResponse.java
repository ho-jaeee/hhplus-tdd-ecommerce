package kr.hhplus.be.server.mockProduct;

public record ProductResponse(
        Long productId,
        String name,
        Long price,
        int stock
) {}
