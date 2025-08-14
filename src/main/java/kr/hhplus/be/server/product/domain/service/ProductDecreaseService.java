package kr.hhplus.be.server.product.domain.service;


import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDecreaseService {

    private final ProductRepository productRepository;


    public void decreaseStock(long productId, int amount) {
        ProductJPA product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        product.decreaseQuantity(amount);
    }

    public void decreaseStocks(List<OrderItemCommand> items) {
        // 데드락 예방 차원에서 정렬(일관된 처리 순서)
        List<OrderItemCommand> sorted = items.stream()
                .sorted(Comparator.comparingLong(OrderItemCommand::productId))
                .toList();

        for (OrderItemCommand i : sorted) {
            ProductJPA p = productRepository.findById(i.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + i.productId()));
            p.decreaseQuantity(i.quantity());
        }
        productRepository.flush();
    }

}

