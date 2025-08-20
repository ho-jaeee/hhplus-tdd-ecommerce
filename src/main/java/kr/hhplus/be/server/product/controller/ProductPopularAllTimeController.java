package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import kr.hhplus.be.server.product.usecase.ProductPopularRankingAllTimeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductPopularAllTimeController {

    private final ProductPopularRankingAllTimeUseCase useCase;

    /**
     * 전체기간
     * 예) GET /products/rank/all-time?n=20 top-20
     *        default n =10
     */

    @GetMapping("/rank/all-time")
    public List<ProductPopularDto> getAllTime(@RequestParam(required = false) Integer n) {
        return useCase.execute(n);
    }
}
