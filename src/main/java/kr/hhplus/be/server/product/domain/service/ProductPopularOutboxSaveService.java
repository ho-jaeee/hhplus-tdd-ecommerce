package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularOutboxDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductPopularOutboxSaveService {
    void writeForOrder(LocalDateTime orderCreatedAt, List<ProductPopularOutboxDto> items);
}
