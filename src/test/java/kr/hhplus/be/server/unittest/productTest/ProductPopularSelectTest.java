package kr.hhplus.be.server.unittest.productTest;


import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository; // ⬅︎ 실제 타입
import kr.hhplus.be.server.product.domain.service.ProductPopularSelectServiceImpl;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository.PopularAggRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductPopularSelectTest {

    private static final String ALL_CACHE_KEY = "product:popular:all:cache";

    @Mock RedissonClient redisson;
    @Mock RKeys keys;
    // 제네릭 충돌 피하려면 raw 또는 Object 권장
    @Mock RScoredSortedSet zsetAll;

    @Mock ProductPopularRepository productPopularRepository;
    @Mock ProductRepository productRepository; // ⬅︎ 실제 리포지토리 Mock

    @InjectMocks ProductPopularSelectServiceImpl service;

    @Test
    @DisplayName("전체기간 TOP10 - 캐시 미스: DB 10건을 점수 내림차순으로 채워서 반환한다")
    void getTopAll_top10_cacheMiss_returnsSorted10() {
        int limit = 10;

        // Redis 캐시 미스
        when(redisson.getKeys()).thenReturn(keys);
        when(keys.countExists(ALL_CACHE_KEY)).thenReturn(0L);
        when(redisson.getScoredSortedSet(ALL_CACHE_KEY)).thenReturn((RScoredSortedSet) zsetAll);

        // DB 집계 결과 10건
        List<PopularAggRow> topRows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            topRows.add(new PopularAggRow(1000L + i, 100L - i));
        }
        when(productPopularRepository.findAllTimeTop(limit)).thenReturn(topRows);

        // 상품명 스텁
        for (int i = 0; i < 100; i++) {
            long pid = 1000L + i;
            when(productRepository.findProductNameById(pid)).thenReturn("P" + pid);
        }

        // ZSET에서 최종 읽힐 값
        List<ScoredEntry<Long>> entries = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            entries.add(new ScoredEntry<>(Double.valueOf(100 - i), 1000L + i));
        }
        when(zsetAll.entryRangeReversed(0, 9)).thenReturn((List) entries);

        // when
        List<ProductPopularDto> out = service.getTopAll(limit);


// productId -> score 맵 구조로 변환
        Map<Long, Long> idScoreMap = out.stream()
                .collect(Collectors.toMap(
                        ProductPopularDto::productId,
                        ProductPopularDto::score,
                        (a, b) -> a, // 중복키 있을 경우 첫 값 유지
                        LinkedHashMap::new // 순서 보존
                ));

        System.out.println("출력된 아이디-점수 매핑: " + idScoreMap);

        // then
        assertThat(out).hasSize(100);
        assertThat(out.get(0).productId()).isEqualTo(1000L);
        assertThat(out.get(0).productName()).isEqualTo("P1000");
        assertThat(out.get(0).score()).isEqualTo(100L);

        verify(productPopularRepository).findAllTimeTop(limit);
        verify(zsetAll).clear();
        verify(zsetAll, atLeast(10)).addScore(anyLong(), anyDouble());
        verify(keys).expire(eq(ALL_CACHE_KEY), anyLong(), eq(TimeUnit.SECONDS));
        verifyNoMoreInteractions(productPopularRepository);
    }
}