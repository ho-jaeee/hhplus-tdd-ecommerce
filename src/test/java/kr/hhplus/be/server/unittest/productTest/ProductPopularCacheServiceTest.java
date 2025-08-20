package kr.hhplus.be.server.unittest.productTest;

import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheServiceImpl;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPopularCacheServiceTest {
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ZSetOperations<String, String> zsetOps;
    @Mock private HashOperations<String, Object, Object> hashOps;

    private ProductPopularCacheServiceImpl sut;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DAILY_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @BeforeEach
    void setUp() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(redis.opsForZSet()).thenReturn(zsetOps);
        when(redis.opsForHash()).thenReturn(hashOps);

        // 테스트 대상 구현체를 주입하세요.
        sut = new ProductPopularCacheServiceImpl(redis);
    }

    @Test
    void addSalesBatch_firstSeen_increments_scores_puts_meta_and_sets_ttl() {
        // given
        String eventId = "evt-123";
        // 2025-08-21T02:30Z -> KST(UTC+9) = 2025-08-21
        Instant createdAt = Instant.parse("2025-08-21T02:30:00Z");
        List<ProductPopularDto> items = List.of(
                new ProductPopularDto(1001L, "상품A", 2),
                new ProductPopularDto(2002L, "상품B", 1)
        );

        when(valueOps.setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any()))
                .thenReturn(Boolean.TRUE);

        // executePipelined 콜백 실행 스텁
        when(redis.executePipelined(any(RedisCallback.class))).thenAnswer(inv -> {
            RedisCallback<?> cb = inv.getArgument(0);
            cb.doInRedis(mock(RedisConnection.class));
            return List.of();
        });
        when(redis.executePipelined(any(SessionCallback.class))).thenAnswer(inv -> {
            SessionCallback<?> cb = inv.getArgument(0);
            cb.execute(redis);
            return List.of();
        });

        String dailyKey = "rank:product:sales:daily:" +
                DAILY_FMT.format(LocalDateTime.ofInstant(createdAt, KST));

        // when
        sut.addSalesBatch(items, createdAt, eventId);

        // then: 점수 증분
        verify(zsetOps).incrementScore(dailyKey, "1001", 2.0);
        verify(zsetOps).incrementScore(dailyKey, "2002", 1.0);

        // 메타 putIfAbsent
        verify(hashOps).putIfAbsent("product:meta", "1001", "상품A");
        verify(hashOps).putIfAbsent("product:meta", "2002", "상품B");

        // TTL 10일
        verify(redis).expire(eq(dailyKey), eq(Duration.ofDays(10)));

        // 멱등성 키 설정
        verify(valueOps).setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any());
    }

    @Test
    void addSalesBatch_duplicateEvent_doesNothing() {
        // given
        String eventId = "evt-dup";
        Instant createdAt = Instant.parse("2025-08-21T00:00:00Z");
        List<ProductPopularDto> items = List.of(
                new ProductPopularDto(3003L, "상품C", 5)
        );

        when(valueOps.setIfAbsent(eq("idem:order:event:" + eventId), eq("1"), any()))
                .thenReturn(Boolean.FALSE); // 이미 처리됨

        // when
        sut.addSalesBatch(items, createdAt, eventId);

        // then: 어떤 변경도 없어야 함
        verifyNoInteractions(zsetOps);
        verifyNoInteractions(hashOps);
        verify(redis, never()).expire(anyString(), any());
    }
}
