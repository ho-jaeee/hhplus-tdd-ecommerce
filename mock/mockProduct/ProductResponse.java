package kr.hhplus.be.server.mock.mockProduct;

public record ProductResponse(
        Long productId,
        String name,
        Long price,
        int stock
) {}
