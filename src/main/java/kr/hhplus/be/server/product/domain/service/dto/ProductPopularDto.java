package kr.hhplus.be.server.product.domain.service.dto;

public record ProductPopularDto(
        Long productId,   // 상품 ID
        String productName,// 상품 이름 (선택: DB Join/캐시에서 가져올 수 있다면)
        long score       // 누적 판매량 (또는 점수)
) {}
