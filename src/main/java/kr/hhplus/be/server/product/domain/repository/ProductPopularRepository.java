package kr.hhplus.be.server.product.domain.repository;


import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.infrastructure.dto.PopularAggRow;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductPopularRepository {
    ProductPopularJPA save(ProductPopularJPA product);

    Optional<ProductPopularJPA> findByProductIdAndBucketStart(Long productId, LocalDateTime bucketStart);

    List<PopularAggRow> findAllTimeTop(int limit);

    List<PopularAggRow> findTopByWindow(LocalDateTime from, int limit);
}

