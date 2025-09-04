package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.product.domain.service.ProductPopularSelectService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor

public class ProductPopularRankingAllTimeUseCase {

    private final ProductPopularSelectService popularSelectService;

    /**
     * 전체(올타임) Top-N 조회 유스케이스
     * - 파라미터 정규화, 기본값/상한, 로깅/메트릭 포인트 위치
     */
    public List<ProductPopularDto> execute(Integer limit) {
        int n = normalize(limit);
        // 필요 시 멱등/모니터링/권한 체크 등 삽입 지점
        return popularSelectService.getTopAll(n);
    }

    private int normalize(Integer limit) {
        if (limit == null) return 10;
        if (limit <= 0) return 10;
        return Math.min(limit, 100);
    }
}
