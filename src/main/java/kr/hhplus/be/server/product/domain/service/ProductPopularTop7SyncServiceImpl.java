package kr.hhplus.be.server.product.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductPopularTop7SyncServiceImpl implements ProductPopularTop7SyncService {

    private final StringRedisTemplate redis;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final String CACHE_KEY = "rank:product:sales:weekly:cache";
    private static final String META_KEY  = "rank:product:sales:weekly:meta";


    @Override
    public void refreshWeeklyCache() {

            // 1) 7일(오늘 포함) 일간 키 만들기
            LocalDate end = LocalDate.now(KST);
            String[] dayKeys = new String[7];
            for (int i = 0; i < 7; i++) {
                LocalDate d = end.minusDays(i);
                dayKeys[i] = "rank:product:sales:daily:" + d.format(DateTimeFormatter.BASIC_ISO_DATE);
            }
            String first = dayKeys[0];
            List<String> rest = java.util.Arrays.stream(dayKeys).skip(1).toList();

            // 2) 서버에서 합산→캐시에 저장
            redis.opsForZSet().unionAndStore(first, rest, CACHE_KEY);

            // 3) TTL
            int ttlSec = 600; // 10분
            redis.expire(CACHE_KEY, Duration.ofSeconds(ttlSec));

            // 4) 메타(간단)
            Map<String, String> meta = Map.of(
                    "last_updated", OffsetDateTime.now(KST).toString(),
                    "window_start", end.minusDays(6).toString(),
                    "window_end",   end.toString()
            );
            redis.opsForHash().putAll(META_KEY, meta);
            redis.expire(META_KEY, Duration.ofSeconds(ttlSec));
        }

}

