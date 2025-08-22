package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.OutboxStatus;
import kr.hhplus.be.server.product.domain.model.ProductPopularOutboxJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularOutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public class ProductPopularOutboxRepositoryImpl implements ProductPopularOutboxRepository {

    private final SpringDataProductPopularOutboxRepository jpaRepository;

    public ProductPopularOutboxRepositoryImpl(SpringDataProductPopularOutboxRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void saveAll(List<Row> rows) {
        var entities = rows.stream()
                .map(r -> ProductPopularOutboxJPA.builder()
                        .productId(r.productId())
                        .scoreDelta(r.delta())
                        .eventTime(r.eventTime())
                        .status(OutboxStatus.PENDING)
                        .retryCount(0)
                        .build())
                .toList();
        jpaRepository.saveAll(entities);

    }

    @Override
    @Transactional
    public List<Row> findBatchForProcess(Instant now, int limit) {
        return jpaRepository.findBatchForProcess(OutboxStatus.PENDING, now, PageRequest.of(0, limit))
                .stream()
                .map(e -> new Row(e.getId(), e.getProductId(), e.getScoreDelta(), e.getEventTime(),
                        OutboxStatus.valueOf(e.getStatus().name()), e.getRetryCount()))
                .toList();
    }

    @Override
    @Transactional
    public boolean markProcessing(List<Long> ids) {
        int updated = jpaRepository.updateStatusInBulk(ids,
               OutboxStatus.PENDING, OutboxStatus.PROCESSING);
        return updated > 0;
    }

    @Override
    @Transactional
    public void markDone(List<Long> ids) {
        jpaRepository.updateStatusInBulk(ids,
                OutboxStatus.PROCESSING,
                OutboxStatus.DONE);

    }

    @Override
    @Transactional
    public void rescheduleWithBackoff(Long id, int nextRetryCount, Instant nextRetryAt) {
        jpaRepository.reschedule(id, nextRetryCount, nextRetryAt);
    }
}
