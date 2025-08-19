package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductPopularInsertAndUpdateServiceImpl implements ProductPopularInsertAndUpdateService {

    private final ProductPopularRepository popularRepository;

    @Override
    public void addSale(long productId, long quantity, LocalDateTime bucketStart) {

        LocalDateTime bucket = bucketStart.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime now = LocalDateTime.now();


        ProductPopularJPA entity = popularRepository
                .findByProductIdAndBucketStart(productId, bucket)
                .orElseGet(() -> {
                    ProductPopularJPA e = new ProductPopularJPA();
                    e.setProductId(productId);
                    e.setBucketStart(bucket);
                    e.setScore(0L);
                    e.setUpdatedAt(now);
                    return e;
                });
        entity.setScore(entity.getScore() + quantity);
        entity.setUpdatedAt(now);

        popularRepository.save(entity);

    }
}
