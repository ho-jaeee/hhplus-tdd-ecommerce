package kr.hhplus.be.server.product.controller;


import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import kr.hhplus.be.server.product.usecase.ProductPopularRankingDayUseCase;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;


import java.util.List;
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductPopularDayController {
    private final ProductPopularRankingDayUseCase useCase;

    /**
     * 오늘(KST) 일간 Top-N
     * 예) GET /products/rank/today?n=10
     */
    @GetMapping("/rank/today")
    public List<ProductPopularDto> todayTop(@RequestParam(defaultValue = "10") int n) {
        return useCase.getTodayTopN(n);  // 유스케이스 내부에서 n 정규화(기본값/상한) 처리
    }

     /**
     * 최근 7일(KST, 오늘 포함) Top-N
     * 예) GET /products/rank/weekly?n=10
     */
    @GetMapping("/rank/weekly")
    public List<ProductPopularDto> weeklyTop(@RequestParam(defaultValue = "10") int n) {
        return useCase.getLast7DaysTopN(n);
    }
}
