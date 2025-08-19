package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductPopularDaySelectService {
    List<ProductPopularDto> getTopDay(LocalDateTime until, int limit);
}
