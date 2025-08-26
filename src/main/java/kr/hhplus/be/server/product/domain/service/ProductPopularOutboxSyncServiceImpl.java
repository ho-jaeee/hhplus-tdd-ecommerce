package kr.hhplus.be.server.product.domain.service;


import kr.hhplus.be.server.product.domain.repository.ProductPopularOutboxRepository;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPopularOutboxSyncServiceImpl implements ProductPopularOutboxSyncService {

    private final ProductPopularOutboxRepository outboxRepository;
    private final ProductPopularCacheService cacheService;


    @Override
    @Transactional
    public int syncBatch(int limit) {
        // 1) 읽기
        List<ProductPopularOutboxRepository.Row> batch = outboxRepository.findBatchForProcess(Instant.now(), limit);
        if (batch.isEmpty()) return 0;

        // 2) 선점(PENDING -> PROCESSING). 경합 시 0 가능.
        var ids = batch.stream().map(ProductPopularOutboxRepository.Row::id).toList();
        boolean locked = outboxRepository.markProcessing(ids);
        if (!locked) return 0;

        // 3) 캐시 반영
        int success = 0;
        for (ProductPopularOutboxRepository.Row r : batch) {
            try {
                // 멱등키는 outbox id 기반으로 안전하게 구성
                String idemEvent = "obx-" + r.id();

                // score 증가치(delta)만큼 일간 키에 반영
                cacheService.addSalesBatch(
                        List.of(new ProductPopularDto(r.productId(), null, r.delta())),
                        r.eventTime(), // UTC -> 서비스 내부에서 KST 날짜로 변환해서 키 생성
                        idemEvent
                );
                success++;
            } catch (Exception ex) {
                // 4) 실패 항목만 backoff
                int nextRetry = r.retryCount() + 1;
                long sec = Math.min(60, (long) Math.pow(2, Math.min(nextRetry, 5))); // 1,2,4,8,16,32, cap 60s
                outboxRepository.rescheduleWithBackoff(r.id(), nextRetry, Instant.now().plusSeconds(sec));
            }
        }

        // 5) 성공 항목 일괄 DONE
        outboxRepository.markDone(ids);
        return success;
    }
}

