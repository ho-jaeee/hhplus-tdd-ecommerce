package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        // 최근 7일(끝 날짜 포함) 키 수집
        String[] dayKeys = new String[7];
        for (int i = 0; i < 7; i++) {
            LocalDate d = kstEndDateInclusive.minusDays(i);
            dayKeys[i] = "rank:product:sales:daily:" + DAILY_FMT.format(d);
        }

        // 임시 키에 합산
        String destKey = "rank:product:sales:7d:" + DAILY_FMT.format(kstEndDateInclusive);
        // 첫 번째 키를 기준으로 합산
        String first = dayKeys[0];
        List<String> rest = java.util.Arrays.stream(dayKeys).skip(1).toList();
        redis.opsForZSet().unionAndStore(first, rest, destKey);
        // 임시 키는 짧게 만료
        redis.expire(destKey, Duration.ofMinutes(5));

        var tuples = redis.opsForZSet().reverseRangeWithScores(destKey, 0, Math.max(0, n - 1));
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

}


