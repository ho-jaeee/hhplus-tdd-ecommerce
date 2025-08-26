package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPopularCacheServiceImpl implements ProductPopularCacheService {

    private final StringRedisTemplate redis;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DAILY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public void addSalesBatch(List<ProductPopularDto> items, Instant createdAt, String eventId) {

            String dailyKey = "rank:product:sales:daily:" +
                    DAILY_FMT.format(LocalDateTime.ofInstant(createdAt, KST));
            String idemKey = "idem:order:event:" + eventId;

            // 멱등성: 같은 이벤트 두 번 반영 방지
            Boolean firstSeen = redis.opsForValue().setIfAbsent(idemKey, "1", Duration.ofHours(24));
            if (Boolean.FALSE.equals(firstSeen)) return;

            redis.executePipelined((RedisCallback<Object>) conn -> {
                items.forEach(it -> {
                    String member = it.productId().toString();

                    // 판매 수량 누적
                    redis.opsForZSet().incrementScore(dailyKey, member, it.score());


                    // 상품 이름은 메타 정보로 보관
                    redis.opsForHash().putIfAbsent(
                            "product:meta",
                            it.productId().toString(),
                            it.productName()
                    );
                });
                // 일간 키 TTL 10일
                redis.expire(dailyKey, Duration.ofDays(10));
                return null;
            });
        }

    @Override
    public List<ProductPopularDto> getDailyTopN(LocalDate kstDate, int n) {
        String key = "rank:product:sales:daily:" + DAILY_FMT.format(kstDate);
        var tuples = redis.opsForZSet().reverseRangeWithScores(key, 0, Math.max(0, n - 1));
        if (tuples == null || tuples.isEmpty()) return List.of();

        return tuples.stream()
                .map(t -> {
                    String pid = t.getValue();
                    Double score = t.getScore();
                    Object name = redis.opsForHash().get("product:meta", pid);
                    return new ProductPopularDto(Long.valueOf(pid), name == null ? null : name.toString(),
                            (long) (score == null ? 0.0 : score));
                })
                .toList();
    }

    @Override
    public List<ProductPopularDto> getLast7DaysTopN(LocalDate kstEndDateInclusive, int n) {
        final String CACHE_KEY = "rank:product:sales:weekly:cache";
        final int MAX_N = 100;
        final int limit = Math.min(Math.max(n, 1), MAX_N);

        try {
            // 1) 캐시 조회
            var tuplesSet = redis.opsForZSet().reverseRangeWithScores(CACHE_KEY, 0, limit - 1);

            // 2) 캐시 없거나 비었으면 -> 새로 생성
            if (tuplesSet == null || tuplesSet.isEmpty()) {
                tuplesSet = rebuildWeeklyCache(kstEndDateInclusive, limit);
                if (tuplesSet == null || tuplesSet.isEmpty()) return List.of();
            }

            // 순서 보존
            var tuples = new java.util.ArrayList<>(tuplesSet);

            // product:meta HMGET
            var pids = tuples.stream().map(t -> t.getValue()).toList();
            var names = redis.opsForHash().multiGet("product:meta", new java.util.ArrayList<>(pids));

            // 매핑
            var out = new java.util.ArrayList<ProductPopularDto>(tuples.size());
            for (int i = 0; i < tuples.size(); i++) {
                var t = tuples.get(i);
                String pidStr = t.getValue();
                Double score = t.getScore();

                long pid;
                try {
                    pid = Long.parseLong(pidStr);
                } catch (NumberFormatException e) {
                    continue; // 숫자 아닌 멤버는 스킵
                }

                String name = (names != null && i < names.size() && names.get(i) != null)
                        ? names.get(i).toString()
                        : null;

                long cnt = (score == null) ? 0L : Math.round(score);
                out.add(new ProductPopularDto(pid, name, cnt));
            }
            return out;
        } catch (Exception e) {
            // Redis 연결 등 예외 시 안전하게 빈 리스트 반환
            log.warn("getLast7DaysTopN failed: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private Set<ZSetOperations.TypedTuple<String>> rebuildWeeklyCache(LocalDate endDate, int limit) {
        String[] dayKeys = new String[7];
        for (int i = 0; i < 7; i++) {
            LocalDate d = endDate.minusDays(i);
            dayKeys[i] = "rank:product:sales:daily:" + DAILY_FMT.format(d);
        }
        String first = dayKeys[0];
        List<String> rest = java.util.Arrays.stream(dayKeys).skip(1).toList();

        // 합산해서 캐시에 저장
        redis.opsForZSet().unionAndStore(first, rest, "rank:product:sales:weekly:cache");
        redis.expire("rank:product:sales:weekly:cache", Duration.ofMinutes(10));

        // 다시 읽어서 반환
        return redis.opsForZSet().reverseRangeWithScores("rank:product:sales:weekly:cache", 0, limit - 1);
    }

}


