package kr.hhplus.be.server.productTest.domain.service;

import kr.hhplus.be.server.productTest.domain.model.ProductJPA;
import kr.hhplus.be.server.productTest.domain.repository.ProductRepository;
import kr.hhplus.be.server.productTest.usecase.ProductFindUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductFindService implements ProductFindUseCase {

    private final ProductRepository productRepository;

    @Override
    public ProductJPA findProducts(Long productId) {

        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 제품이 존재하지 않습니다: " + productId));
    }
}

