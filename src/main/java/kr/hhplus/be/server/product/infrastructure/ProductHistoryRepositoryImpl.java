package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.repository.ProductHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductHistoryRepositoryImpl implements ProductHistoryRepository {

    private final Map<Long, ProductHistoryJPA> table = new HashMap<>();

    @Override
    public ProductHistoryJPA insert(ProductHistoryJPA history) {
        table.put(history.getId(), history);
        return history;
    }

    @Override
    public List<ProductHistoryJPA> findByProductId(Long productId) {
        return table.values().stream()
                .filter(h -> h.getProductId().equals(productId))
                .toList();
    }

    @Override
    public List<ProductHistoryJPA> findAll() {
        return new ArrayList<>(table.values());
    }
}
