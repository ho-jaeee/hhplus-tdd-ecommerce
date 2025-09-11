package kr.hhplus.be.server.product.domain.service;



import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCheckService {


    private final ProductRepository productRepository;

    public void stockCheck(Long productId, int requestQuantity) {
        ProductJPA product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        if (!product.stockCheck(requestQuantity)) {
            throw new IllegalStateException("재고 부족");
        }
    }

    public void validateAllStock(List<OrderItemCommand> items) {
        for (OrderItemCommand item : items) {
            stockCheck(item.productId(), item.quantity());
        }
    }
}
