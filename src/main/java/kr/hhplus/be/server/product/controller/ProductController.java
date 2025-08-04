package kr.hhplus.be.server.product.controller;

import kr.hhplus.be.server.product.controller.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.usecase.ProductFindAllUseCase;
import kr.hhplus.be.server.product.usecase.ProductFindUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductFindAllUseCase productFindAllUseCase;
    private final ProductFindUseCase productFindUseCase;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAllProducts() {
        List<ProductResponse> responseList = productFindAllUseCase.findAllProducts()
                .stream()
                .map(ProductResponse::from)
                .toList();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable Long id) {
        ProductJPA product = productFindUseCase.findProducts(id);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

}
