package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductPopularSelectServiceImpl implements ProductPopularSelectService {

    private final RedissonClient redisson;
    private final ProductPopularRepository productPopularRepository;
    private final ProductRepository productRepository;
    private static final String ALL_CACHE_KEY = "product:popular:all:cache";
    private static final Duration ALL_CACHE_TTL = Duration.ofSeconds(60);


    @Override
    public List<ProductPopularDto> getTopAll(int limit) {
        int n = (limit <= 0) ? 10 : Math.min(limit, 100);

        RScoredSortedSet<Long> z = redisson.getScoredSortedSet(ALL_CACHE_KEY);
        RKeys keys = redisson.getKeys();

        boolean hit = keys.countExists(ALL_CACHE_KEY) > 0 && z.size() > 0;
        if (!hit) {
            // 캐시 미스 → DB 집계 상위 N 로드 후 ZSET 리빌드
            var top = productPopularRepository.findAllTimeTop(n); // List<ProductPopularRepository.PopularAggRow>
            z.clear();
            for (var row : top) {
                z.addScore(row.productId(), row.total().doubleValue());
            }
            if (!top.isEmpty()) {
                keys.expire(ALL_CACHE_KEY, ALL_CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
            }
        }



        // Redis ZSET에서 TOP-N 읽고 DTO 매핑 (productName 필요 시 채워 넣으세요)
        List<ScoredEntry<Long>> entries = new ArrayList<>(z.entryRangeReversed(0, n - 1));
        return entries.stream()
                .map(e -> new ProductPopularDto(e.getValue(),
                                                                productRepository.findProductNameById(e.getValue()),
                                                                e.getScore().longValue()))
                .toList();

    }
}
