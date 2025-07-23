package kr.hhplus.be.server.productTest.usecase;

import kr.hhplus.be.server.productTest.domain.model.ProductJPA;

import java.util.List;

public interface ProductFindAllUseCase {
    List<ProductJPA> findAllProducts();
}
