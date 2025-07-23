package kr.hhplus.be.server.productTest.domain.repository;

import kr.hhplus.be.server.productTest.domain.model.ProductJPA;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    ProductJPA insertOrUpdate(ProductJPA product);

    Optional<ProductJPA> findByProductId(Long productId);

    List<ProductJPA> findAll();

}
