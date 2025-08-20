package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.product.domain.model.OutboxStatus;

import java.time.Instant;
import java.util.List;

public interface ProductPopularOutboxRepository {

    record Row(Long id, Long productId, long delta, Instant eventTime,
               OutboxStatus status, int retryCount) {}

    void saveAll(List<Row> rows);

    List<Row> findBatchForProcess(Instant now, int limit);

    boolean markProcessing(List<Long> ids);

    void markDone(List<Long> ids);

    void rescheduleWithBackoff(Long id, int nextRetryCount, Instant nextRetryAt);
}
