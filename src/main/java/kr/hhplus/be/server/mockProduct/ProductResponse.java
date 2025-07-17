package kr.hhplus.be.server;

public record ProductResponse(
        Long productId,
        String name,
        Long price,
        int stock
) {}
