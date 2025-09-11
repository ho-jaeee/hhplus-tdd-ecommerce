package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;

import java.util.List;

public interface ProductPopularSelectService {
    List<ProductPopularDto> getTopAll(int limit);
}
