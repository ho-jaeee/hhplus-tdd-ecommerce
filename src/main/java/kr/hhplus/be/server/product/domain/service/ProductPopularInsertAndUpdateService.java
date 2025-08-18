package kr.hhplus.be.server.product.domain.service;

import java.time.LocalDateTime;

public interface ProductPopularInsertAndUpdateService {

    void addSale(long productId, long qty, LocalDateTime bucketStart);
}
