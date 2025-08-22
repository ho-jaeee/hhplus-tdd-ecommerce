package kr.hhplus.be.server.unittest.productTest;



import kr.hhplus.be.server.product.domain.service.ProductPopularCacheServiceImpl;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductPopularCacheTopNTest {
    @Mock private StringRedisTemplate redis;
    @Mock private ZSetOperations<String, String> zsetOps;
    @Mock private HashOperations<String, Object, Object> hashOps;

    private ProductPopularCacheServiceImpl sut;


    @BeforeEach
    void setUp() {
        when(redis.opsForZSet()).thenReturn(zsetOps);
        sut = new ProductPopularCacheServiceImpl(redis);
    }

    @Test
    void getDailyTopN_returns_sorted_by_score_with_names() {
        // given: 2025-08-21 일간 키
        String key = "rank:product:sales:daily:20250821";

        // ZSET 상위 3개 튜플 (점수 내림차순 반환 가정)
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("2002", 7.0));
        tuples.add(new DefaultTypedTuple<>("1001", 5.0));
        tuples.add(new DefaultTypedTuple<>("3003", 2.0));

        when(zsetOps.reverseRangeWithScores(eq(key), eq(0L), eq(2L))).thenReturn(tuples);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.get("product:meta", "2002")).thenReturn("상품B");
        when(hashOps.get("product:meta", "1001")).thenReturn("상품A");
        when(hashOps.get("product:meta", "3003")).thenReturn("상품C");

        // when
        List<ProductPopularDto> top3 = sut.getDailyTopN(LocalDate.of(2025, 8, 21), 3);

        // then
        assertThat(top3).hasSize(3);
        assertThat(top3.get(0).productId()).isEqualTo(2002L);
        assertThat(top3.get(0).productName()).isEqualTo("상품B");
        assertThat(top3.get(0).score()).isEqualTo(7L);

        assertThat(top3.get(1).productId()).isEqualTo(1001L);
        assertThat(top3.get(1).productName()).isEqualTo("상품A");
        assertThat(top3.get(1).score()).isEqualTo(5L);

        assertThat(top3.get(2).productId()).isEqualTo(3003L);
        assertThat(top3.get(2).productName()).isEqualTo( "상품C");
        assertThat(top3.get(2).score()).isEqualTo(2L);
    }

    @Test
    void getDailyTopN_when_empty_returns_empty_list() {
        String key = "rank:product:sales:daily:20250822";
        when(zsetOps.reverseRangeWithScores(eq(key), eq(0L), eq(4L))).thenReturn(Set.of());

        List<ProductPopularDto> top5 = sut.getDailyTopN(LocalDate.of(2025, 8, 22), 5);
        assertThat(top5).isEmpty();
    }

    @Test
    void getLast7DaysTopN_unions_daily_keys_and_returns_topn() {
        // given: 끝일자 2025-08-21, 최근7일 dest 키
        String destKey = "rank:product:sales:7d:20250821";

        // unionAndStore 호출 추적
        ArgumentCaptor<String> firstKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<String>> restKeys = ArgumentCaptor.forClass(List.class);
        doReturn(10L).when(zsetOps).unionAndStore(firstKey.capture(), restKeys.capture(), eq(destKey));

        // union 결과에서 역순 TOP 2
        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(new DefaultTypedTuple<>("1001", 12.0));
        tuples.add(new DefaultTypedTuple<>("2002", 10.0));
        when(zsetOps.reverseRangeWithScores(eq(destKey), eq(0L), eq(1L))).thenReturn(tuples);
        when(redis.opsForHash()).thenReturn(hashOps);
        when(hashOps.get("product:meta", "1001")).thenReturn("상품A");
        when(hashOps.get("product:meta", "2002")).thenReturn("상품B");

        // when
        List<ProductPopularDto> top2 = sut.getLast7DaysTopN(LocalDate.of(2025, 8, 21), 2);

        // then: union이 제대로 호출되었는지(키 구성)
        String fk = firstKey.getValue();
        List<String> rk = restKeys.getValue();
        // 첫 키는 20250821
        assertThat(fk).isEqualTo("rank:product:sales:daily:20250821");
        // 나머지 6개가 20250820 … 20250815 순서로 들어왔는지 (여기선 개수만 확인)
        assertThat(rk).hasSize(6);

        // 최종 결과 검증
        assertThat(top2).hasSize(2);
        assertThat(top2.get(0).productId()).isEqualTo(1001L);
        assertThat(top2.get(0).productName()).isEqualTo("상품A");
        assertThat(top2.get(0).score()).isEqualTo(12L);

        assertThat(top2.get(1).productId()).isEqualTo(2002L);
        assertThat(top2.get(1).productName()).isEqualTo("상품B");
        assertThat(top2.get(1).score()).isEqualTo(10L);

        // expire(destKey, 5분) 호출 여부
        verify(redis).expire(eq(destKey), any());
    }
}
