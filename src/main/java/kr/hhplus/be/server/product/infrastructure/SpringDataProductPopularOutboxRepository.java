package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.OutboxStatus;
import kr.hhplus.be.server.product.domain.model.ProductPopularOutboxJPA;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataProductPopularOutboxRepository extends JpaRepository
        <ProductPopularOutboxJPA, Long> {
    @Query("""
     select o from ProductPopularOutboxJPA o
      where o.status = :status
        and (o.nextRetryAt is null or o.nextRetryAt <= :now)
      order by o.id asc
  """)
    List<ProductPopularOutboxJPA> findBatchForProcess(@Param("status") OutboxStatus status,
                                                      @Param("now") Instant now,
                                                      Pageable pageable);

    @Modifying
    @Query("""
     update ProductPopularOutboxJPA o
        set o.status = :to
      where o.id in :ids and o.status = :from
  """)
    int updateStatusInBulk(@Param("ids") List<Long> ids,
                           @Param("from") OutboxStatus from,
                           @Param("to") OutboxStatus to);

    @Modifying
    @Query("""
     update ProductPopularOutboxJPA o
        set o.status = 'PENDING',
            o.retryCount = :retry,
            o.nextRetryAt = :nextAt
      where o.id = :id
  """)
    int reschedule(@Param("id") Long id,
                   @Param("retry") int retry,
                   @Param("nextAt") Instant nextAt);

}
