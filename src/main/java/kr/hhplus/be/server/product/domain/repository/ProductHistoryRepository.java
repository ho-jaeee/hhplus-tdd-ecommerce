package kr.hhplus.be.server.product.domain.repository;


import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;

import java.util.List;

public interface ProductHistoryRepository{

    ProductHistoryJPA save(ProductHistoryJPA productHistory);
    List<ProductHistoryJPA> findAll();

}
