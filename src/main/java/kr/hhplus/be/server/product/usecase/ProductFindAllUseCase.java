package kr.hhplus.be.server.product.usecase;

import kr.hhplus.be.server.product.domain.model.ProductJPA;

import java.util.List;

public interface ProductFindAllUseCase {
    List<ProductJPA> findAllProducts();
}
