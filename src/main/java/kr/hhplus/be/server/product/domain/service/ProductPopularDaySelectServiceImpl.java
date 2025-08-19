package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPopularDaySelectServiceImpl implements  ProductPopularDaySelectService  {
    @Override
    public List<ProductPopularDto> getTopDay(LocalDateTime until, int limit) {
        return List.of();
    }
}
