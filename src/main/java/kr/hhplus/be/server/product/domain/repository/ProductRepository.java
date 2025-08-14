package kr.hhplus.be.server.product.domain.repository;


import kr.hhplus.be.server.product.domain.model.ProductJPA;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<ProductJPA> findById(long id);
    ProductJPA save(ProductJPA product);
    List<ProductJPA> findAll();
    void flush();
    void deleteAll(); //테스트용



}
