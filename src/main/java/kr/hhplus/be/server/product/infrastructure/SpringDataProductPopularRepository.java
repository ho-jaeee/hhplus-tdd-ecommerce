package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.infrastructure.dto.PopularAggRow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataProductPopularRepository extends JpaRepository<ProductPopularJPA, Long> {
    Optional<ProductPopularJPA> findByProductIdAndBucketStart(Long productId, LocalDateTime bucketStart);


    /*** 전기간 TOP-N***/
    @Query("""
        select new kr.hhplus.be.server.product.infrastructure.dto.PopularAggRow(
            p.productId, sum(p.score)
        )
        from ProductPopularJPA p
        group by p.productId
        order by sum(p.score) desc, p.productId asc
    """)
    List<PopularAggRow> findAllTimeTop(Pageable pageable);

    /*** 윈도우(예: 24h/72h) TOP-N ***/
    @Query("""
        select new kr.hhplus.be.server.product.infrastructure.dto.PopularAggRow(
            p.productId, sum(p.score)
        )
        from ProductPopularJPA p
        where p.bucketStart >= :from
        group by p.productId
        order by sum(p.score) desc, p.productId asc
    """)
    List<PopularAggRow> findTopByWindow(@Param("from") LocalDateTime from, Pageable pageable);

}
