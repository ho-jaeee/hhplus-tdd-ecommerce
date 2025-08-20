package kr.hhplus.be.server.product.domain.service;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    }

