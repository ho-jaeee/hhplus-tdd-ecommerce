package kr.hhplus.be.server.product.domain.service;

import java.time.LocalDateTime;

public interface ProductPopularSaveService {

    void addSale(long productId, long qty, LocalDateTime bucketStart);
}
