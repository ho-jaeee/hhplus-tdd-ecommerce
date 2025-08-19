package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.infrastructure.dto.PopularAggRow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ProductPopularRepositoryImpl implements ProductPopularRepository {

    private final SpringDataProductPopularRepository jpaRepository;

    public ProductPopularRepositoryImpl(SpringDataProductPopularRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProductPopularJPA save(ProductPopularJPA product) {
        return jpaRepository.save(product);
    }

    @Override
    public Optional<ProductPopularJPA> findByProductIdAndBucketStart(Long productId, LocalDateTime bucketStart) {
        return jpaRepository.findByProductIdAndBucketStart(productId, bucketStart);
    }

    @Override
    public List<PopularAggRow> findAllTimeTop(int limit) {
        int n = Math.max(1, limit);
        return jpaRepository.findAllTimeTop(PageRequest.of(0, n));
    }

    @Override
    public List<PopularAggRow> findTopByWindow(LocalDateTime from, int limit) {
        Objects.requireNonNull(from, "'from' must not be null");
        int n = Math.max(1, limit);
        return jpaRepository.findTopByWindow(from, PageRequest.of(0, n));
    }
}
