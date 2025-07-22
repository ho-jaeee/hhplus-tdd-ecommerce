package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final Map<Long, ProductJPA> table = new HashMap<>();

    @Override
    public ProductJPA insertOrUpdate(ProductJPA product) {
        table.put(product.getProductId(), product);
        return product;
    }

    @Override
    public Optional<ProductJPA> findByProductId(Long productId) {
       return Optional.ofNullable(table.get(productId));
    }

    @Override
    public List<ProductJPA> findAll() {
        return new ArrayList<>(table.values());
    }
}
