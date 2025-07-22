package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.product.domain.model.ProductJPA;

public interface ProductFindUseCase {
    ProductJPA findProducts(Long productId);
}
