package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;

import java.util.List;

public interface ProductHistoryRepository {

    ProductHistoryJPA insert(ProductHistoryJPA history);

    List<ProductHistoryJPA> findByProductId(Long productId);

    List<ProductHistoryJPA> findAll();
}
