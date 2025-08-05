package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final SpringDataProductRepository jpaRepository;

    public ProductRepositoryImpl(SpringDataProductRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ProductJPA> findById(long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public ProductJPA save(ProductJPA product) {
        return jpaRepository.save(product);
    }

    @Override
    public List<ProductJPA> findAll() {
        return jpaRepository.findAll();
    }
}
