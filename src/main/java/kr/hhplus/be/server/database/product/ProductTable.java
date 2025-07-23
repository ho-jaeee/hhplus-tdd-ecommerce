package kr.hhplus.be.server.database.product;


import kr.hhplus.be.server.productTest.domain.model.ProductJPA;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProductTable {

    private final Map<Long, ProductJPA> table = new HashMap<>();

    public ProductJPA insertOrUpdate(ProductJPA product) {
        table.put(product.getProductId(), product);
        return product;
    }

    public Optional<ProductJPA> findByProductId(long ProductId) {
        return Optional.ofNullable(table.get(ProductId));
    }

    public List<ProductJPA> findAll() {
        return new ArrayList<>(table.values());
    }
}
