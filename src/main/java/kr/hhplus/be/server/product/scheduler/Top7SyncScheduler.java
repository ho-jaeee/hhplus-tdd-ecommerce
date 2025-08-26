package kr.hhplus.be.server.product.scheduler;

import kr.hhplus.be.server.product.domain.service.ProductPopularTop7SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class Top7SyncScheduler {

    private final ProductPopularTop7SyncService cacheService;

    @Scheduled(fixedDelayString = "${popular.rank.weekly.fixedDelayMillis:300000}")
    public void refreshWeekly() {
        try {
            cacheService.refreshWeeklyCache();
            log.debug("Weekly rank cache refreshed");
        } catch (Exception e) {
            log.warn("Weekly rank cache refresh failed: {}", e.getMessage(), e);
        }
    }
}
