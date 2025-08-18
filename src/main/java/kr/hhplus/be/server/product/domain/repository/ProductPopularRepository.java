package kr.hhplus.be.server.product.domain.repository;


import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductPopularRepository {
    ProductPopularJPA save(ProductPopularJPA product);
    Optional<ProductPopularJPA> findByProductIdAndBucketStart(Long productId, LocalDateTime bucketStart);

    List<PopularAggRow> findAllTimeTop(int limit);

    record PopularAggRow(Long productId, Long total) {}
}
