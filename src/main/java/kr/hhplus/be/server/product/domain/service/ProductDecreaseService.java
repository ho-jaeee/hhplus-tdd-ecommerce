package kr.hhplus.be.server.product.domain.service;


import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    public void decreaseStocksWithRetry(List<OrderItemCommand> items) {
        final int maxRetry = 3;
        final long[] backoffMs = {50L, 100L, 200L};

        List<OrderItemCommand> sorted = items.stream()
                .sorted(Comparator.comparingLong(OrderItemCommand::productId))
                .toList();

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                decreaseStocksOnce(sorted);   // 성공하면 종료
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                log.warn("[stock] optimistic conflict attempt={}, msg={}", attempt, ex.getMessage());
                if (attempt == maxRetry) throw ex;
                sleep(backoffMs[attempt - 1]);
            }
        }
    }


    protected void decreaseStocksOnce(List<OrderItemCommand> items) {
        for (OrderItemCommand i : items) {
            ProductJPA p = productRepository.findById(i.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + i.productId()));

            p.decreaseQuantity(i.quantity());           // 도메인 규칙
            productRepository.save(p);
            productRepository.flush();  // 즉시 flush → 버전 충돌 조기 감지
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
