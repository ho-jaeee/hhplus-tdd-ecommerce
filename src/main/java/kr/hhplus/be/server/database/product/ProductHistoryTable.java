package kr.hhplus.be.server.database.product;


import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductHistoryTable {

    private final Map<Long, ProductHistoryJPA> table = new HashMap<>();

    // 히스토리 저장 (엔터티에서 ID 생성)
    public ProductHistoryJPA insert(ProductHistoryJPA history) {
        table.put(history.getId(), history);
        return history;
    }

    public List<ProductHistoryJPA> findByProductId(Long productId) {
        return table.values().stream()
                .filter(h -> h.getProductId().equals(productId))
                .toList();
    }

    public List<ProductHistoryJPA> findAll() {
        return new ArrayList<>(table.values());
    }
}
