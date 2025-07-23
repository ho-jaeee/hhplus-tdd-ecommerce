package kr.hhplus.be.server.productTest.usecase;

import kr.hhplus.be.server.productTest.domain.model.ProductJPA;

public interface ProductFindUseCase {
    ProductJPA findProducts(Long productId);
}
