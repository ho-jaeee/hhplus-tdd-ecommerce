package kr.hhplus.be.server.product.scheduler;

import kr.hhplus.be.server.product.domain.service.ProductPopularOutboxSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxSyncScheduler {
    private final ProductPopularOutboxSyncService syncService;

    // fixedDelay: 이전 실행 종료 후 딜레이. 운영 환경에 맞춰 조정.
    @Scheduled(fixedDelayString = "${popular.outbox.sync.fixedDelayMillis:5000}")
    public void run() {
        int processed = syncService.syncBatch(
                Integer.parseInt(System.getProperty("popular.outbox.sync.batchSize", "500"))
        );
        if (processed > 0) {
            log.info("Outbox processed: {}", processed);
        }
    }
}
