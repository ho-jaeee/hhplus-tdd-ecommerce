package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.repository.ProductHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class ProductHistoryRepositoryImpl implements ProductHistoryRepository {

    private final SpringDataProductHistoryRepository jpaRepository;

    public ProductHistoryRepositoryImpl(SpringDataProductHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductHistoryJPA save(ProductHistoryJPA productHistory) {
        return jpaRepository.save(productHistory);
    }

    @Override
    public List<ProductHistoryJPA> findAll() {
        return jpaRepository.findAll();
    }
}
