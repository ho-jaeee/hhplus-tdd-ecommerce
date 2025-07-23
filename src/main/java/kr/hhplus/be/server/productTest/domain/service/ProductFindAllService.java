package kr.hhplus.be.server.productTest.domain.service;

import kr.hhplus.be.server.productTest.domain.model.ProductJPA;
import kr.hhplus.be.server.productTest.domain.repository.ProductRepository;
import kr.hhplus.be.server.productTest.usecase.ProductFindAllUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductFindAllService implements ProductFindAllUseCase {

    private final ProductRepository productRepository;

    public ProductFindAllService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductJPA> findAllProducts() {
        return productRepository.findAll();
    }
}
