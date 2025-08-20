package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface ProductPopularCacheService {
    void addSalesBatch(List<ProductPopularDto> items, Instant createdAt, String eventId);

    List<ProductPopularDto> getDailyTopN(LocalDate kstDate, int n);
    List<ProductPopularDto> getLast7DaysTopN(LocalDate kstEndDateInclusive, int n);
}
