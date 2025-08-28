package kr.hhplus.be.server.product.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPopularRankingDayUseCase {

    private final ProductPopularCacheService cacheService;
    // 정책상 타임존은 KST 고정. 필요하면 설정/프로퍼티 주입으로 바꿔도 됨.
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 오늘(KST) 일간 Top-N
     */
    public List<ProductPopularDto> getTodayTopN(int n) {
        int topN = sanitizeTopN(n);
        LocalDate today = LocalDate.now(KST);
        return cacheService.getDailyTopN(today, topN);
    }

    /**
     * 최근 7일(KST, 오늘 포함) Top-N
     */
    public List<ProductPopularDto> getLast7DaysTopN(int n) {
        int topN = sanitizeTopN(n);
        LocalDate end = LocalDate.now(KST);
        return cacheService.getLast7DaysTopN(end, topN);
    }

    private int sanitizeTopN(int n) {
        // 방어: 음수/0 → 0, 과도한 값 상한(예: 100)
        if (n <= 0) return 0;
        return Math.min(n, 100);
    }
}
