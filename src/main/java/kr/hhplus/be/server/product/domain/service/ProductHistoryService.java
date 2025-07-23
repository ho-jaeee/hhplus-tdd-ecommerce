package kr.hhplus.be.server.product.domain.service;


import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.repository.ProductHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductHistoryService {

    private final ProductHistoryRepository repository;

    public void insertHistory(Long productId, Long orderId, ProductHistoryJPA.ChangeType changeType,
                            int quantity, String productName, Long pricePerUnit) {

        ProductHistoryJPA history = ProductHistoryJPA.createWithoutId(
                productId, orderId, changeType, quantity, productName, pricePerUnit
        );
        repository.insert(history);
    }

}
