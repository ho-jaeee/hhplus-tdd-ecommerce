package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPopularSelectServiceImpl implements ProductPopularSelectService {

    private final RedissonClient redisson;
    private final ProductPopularRepository productPopularRepository;
    private final ProductRepository productRepository;

    private static final String ALL_CACHE_KEY  = "product:popular:all:cache"; // ZSET
    private static final String NAME_HASH_KEY  = "product:name";               // HASH (field: productId, value: name)
    private static final Duration ALL_CACHE_TTL = Duration.ofSeconds(600);

    @Override
    public List<ProductPopularDto> getTopAll(int limit) {
        final int n = (limit <= 0) ? 10 : Math.min(limit, 100);

        // 1) 인기 Top-N ZSET 캐시 히트/미스 처리
        RScoredSortedSet<Long> z = redisson.getScoredSortedSet(ALL_CACHE_KEY);
        RKeys keys = redisson.getKeys();

        boolean hit = keys.countExists(ALL_CACHE_KEY) > 0 && z.size() > 0;
        if (!hit) {
            // 캐시 미스 → DB 집계 상위 N 로드 후 ZSET 리빌드
            var top = productPopularRepository.findAllTimeTop(n); // List<ProductPopularRepository.PopularAggRow>
            z.clear();
            for (var row : top) {
                if (row != null && row.productId() != null && row.total() != null) {
                    z.addScore(row.productId(), row.total().doubleValue());
                }
            }
            if (!top.isEmpty()) {
                keys.expire(ALL_CACHE_KEY, ALL_CACHE_TTL.getSeconds(), TimeUnit.SECONDS);
            }
        }

        // 2) Redis ZSET에서 TOP-N 읽기
        List<ScoredEntry<Long>> entries = new ArrayList<>(z.entryRangeReversed(0, n - 1));
        if (entries.isEmpty()) {
            return List.of();
        }

        // 3) productId 목록 추출
        List<Long> productIds = entries.stream()
                .map(ScoredEntry::getValue)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 4) 이름 해시에서 일괄 조회(HMGET)
        RMap<String, String> nameHash = redisson.getMap(NAME_HASH_KEY);
        Set<String> fields = productIds.stream().map(String::valueOf).collect(Collectors.toSet());
        Map<String, String> cachedNames = nameHash.getAll(fields); // field(productId) -> name

        // 5) 해시에 없는 id만 모아 DB에서 한 번에 조회
        List<Long> missingIds = productIds.stream()
                .filter(id -> cachedNames.get(String.valueOf(id)) == null)
                .toList();

        Map<Long, String> dbNames = Collections.emptyMap();
        if (!missingIds.isEmpty()) {
            // 도메인 레포지토리 시그니처에 맞춤: Map<Long,String> 반환
            dbNames = productRepository.findNameByProductId(missingIds);
            if (dbNames != null && !dbNames.isEmpty()) {
                // 6) 해시 보강(HSET 다건 = putAll)
                Map<String, String> toCache = new HashMap<>(dbNames.size());
                for (var e : dbNames.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null) {
                        toCache.put(String.valueOf(e.getKey()), e.getValue());
                    }
                }
                if (!toCache.isEmpty()) {
                    RMapCache<String, String> nameHashCache = redisson.getMapCache(NAME_HASH_KEY);
                    if (nameHashCache != null) {
                        toCache.forEach((k, v) -> nameHashCache.fastPut(k, v, 1, TimeUnit.DAYS));
                    } else {
                        // 최후 폴백: TTL 없이라도 저장하거나, 전체 키 TTL 부여
                        nameHash.putAll(toCache);
                        redisson.getKeys().expire(NAME_HASH_KEY, 1, TimeUnit.DAYS);
                    }
                }
            }
        }

        // 7) DTO 매핑 (캐시 우선, 누락은 DB 조회 결과 사용, 그래도 없으면 null)
        final Map<Long, String> dbNamesFinal = dbNames;

        return entries.stream()
                .map(e -> {
                    Long pid = e.getValue();
                    String name = cachedNames.get(String.valueOf(pid));
                    if (name == null && dbNamesFinal != null) {
                        name = dbNamesFinal.get(pid);
                    }
                    return new ProductPopularDto(
                            pid,
                            name,                          // 해시/DB에서 가져온 상품명
                            e.getScore().longValue()       // 누적 스코어(판매량/가중치 등)
                    );
                })
                .toList();
    }
}
