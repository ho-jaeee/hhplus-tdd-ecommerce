package kr.hhplus.be.server.product.domain.service;

public interface ProductPopularOutboxSyncService {
    int syncBatch(int limit);
}
