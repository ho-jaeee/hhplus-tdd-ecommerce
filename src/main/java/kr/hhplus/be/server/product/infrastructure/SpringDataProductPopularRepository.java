package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataProductPopularRepository extends JpaRepository<ProductPopularJPA, Long> {
    Optional<ProductPopularJPA> findByProductIdAndBucketStart(Long productId, LocalDateTime bucketStart);

    /**
     * 전기간 TOP 집계 — JPQL constructor expression으로
     * 도메인 DTO(ProductPopularRepository.PopularAggRow)를 직접 생성해서 반환
     */
    @Query("""
        select new kr.hhplus.be.server.product.domain.repository.ProductPopularRepository$PopularAggRow(
            p.productId, sum(p.score)
        )
        from ProductPopularJPA p
        group by p.productId
        order by sum(p.score) desc, p.productId asc
    """)
    List<ProductPopularRepository.PopularAggRow> findAllTimeTop(Pageable pageable);
}
