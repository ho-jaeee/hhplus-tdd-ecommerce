package kr.hhplus.be.server.mock.mockProduct;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Product", description = "상품 관련 API")
public class product {

    @Operation(summary = "전체 상품 목록 조회(Mock)", description = "상품 전체 리스트를 반환합니다.")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        // 🔥 Mock 데이터
        List<ProductResponse> products = new ArrayList<>();
        products.add(new ProductResponse(1L, "무선 키보드", 39000L, 15));
        products.add(new ProductResponse(2L, "게이밍 마우스", 47000L, 7));
        products.add(new ProductResponse(3L, "USB-C 허브", 25000L, 30));
        products.add(new ProductResponse(4L, "이어폰", 125000L, 1));

        return ResponseEntity.ok(products);
    }
    @Operation(summary = "인기 상품 조회(Mock)", description = "최근 3일간 가장 많이 판매된 상품 Top 5를 반환합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularProductResponse>> getPopularProducts() {

        List<PopularProductResponse> popularProducts = List.of(
                new PopularProductResponse(10L, "기계식 키보드", 65000L, 198),
                new PopularProductResponse(2L, "게이밍 마우스", 47000L, 153),
                new PopularProductResponse(5L, "27인치 모니터", 219000L, 142),
                new PopularProductResponse(1L, "무선 키보드", 39000L, 128),
                new PopularProductResponse(6L, "노트북 거치대", 18000L, 91)
        );

        return ResponseEntity.ok(popularProducts);
    }

}