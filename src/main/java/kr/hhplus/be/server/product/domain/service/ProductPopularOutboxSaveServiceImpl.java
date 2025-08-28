package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.model.OutboxStatus;
import kr.hhplus.be.server.product.domain.repository.ProductPopularOutboxRepository;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularOutboxDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPopularOutboxSaveServiceImpl implements ProductPopularOutboxSaveService {

    private final ProductPopularOutboxRepository outboxRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Override
    public void writeForOrder(LocalDateTime orderCreatedAt, List<ProductPopularOutboxDto> items) {

        Instant eventTimeUtc = orderCreatedAt.atZone(KST).toInstant();

        var rows = items.stream()
                .map(i -> new ProductPopularOutboxRepository.Row(
                        null,                   // id (AUTO)
                        i.productId(),          // 상품 ID
                        i.quantity(),           // 증가량
                        eventTimeUtc,           // 이벤트 시각(UTC)
                        OutboxStatus.PENDING,   // 초기 상태
                        0                       // retryCount
                ))
                .toList();

        outboxRepository.saveAll(rows);

    }
}
