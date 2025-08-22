package kr.hhplus.be.server.unittest.productTest;



import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.ProductPopularSelectServiceImpl;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import kr.hhplus.be.server.product.infrastructure.dto.PopularAggRow;
import org.hibernate.mapping.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductPopularSelectTest {
    @Mock private RedissonClient redisson;
    @Mock private RScoredSortedSet<Long> zset;
    @Mock private RKeys rkeys;
    @Mock private RMap<String, String> nameHash;
    @Mock private ProductPopularRepository popularRepo;
    @Mock private ProductRepository productRepo;

    @InjectMocks
    private ProductPopularSelectServiceImpl sut;

    @Test
    @DisplayName("캐시 없음 → DB 조회 → ZSET 리빌드 → 이름 해시 조회 → 성공 반환")
    void cacheMiss_thenDbAndRebuild_thenNameHash_thenSuccess() {
        // 0) 레디슨 객체 제네릭 지정해서 스텁
        when(redisson.<Long>getScoredSortedSet("product:popular:all:cache")).thenReturn(zset);
        when(redisson.getKeys()).thenReturn(rkeys);
        when(redisson.<String, String>getMap("product:name")).thenReturn(nameHash);

        // 1) 캐시 미스 상황
        when(rkeys.countExists("product:popular:all:cache")).thenReturn(0L);

        // 2) DB 집계 결과 (상위 2개)
        var row1 = new PopularAggRow(101L, 5L);
        var row2 = new PopularAggRow(202L, 3L);
        when(popularRepo.findAllTimeTop(10)).thenReturn(List.of(row1, row2));

        // 3) 리빌드 후, Top-N 읽기 (score 내림차순)
        when(zset.entryRangeReversed(0, 9)).thenReturn(List.of(
                new ScoredEntry<>(5.0, 101L),
                new ScoredEntry<>(3.0, 202L)
        ));

        // 4) 상품명: 해시에서 일괄 조회(HMGET 효과)
        var expectedFields = new HashSet<>(List.of("101", "202"));
        Map<String, String> cachedNames = new HashMap<>();
        cachedNames.put("101", "아이폰");
        cachedNames.put("202", "갤럭시");
        when(nameHash.getAll(eq(expectedFields))).thenReturn(cachedNames);

        // 실행
        List<ProductPopularDto> result = sut.getTopAll(10);

        // 검증: 반환값
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(101L, result.get(0).productId().longValue());
        Assertions.assertEquals("아이폰", result.get(0).productName());
        Assertions.assertEquals(5L, result.get(0).score());

        Assertions.assertEquals(202L, result.get(1).productId().longValue());
        Assertions.assertEquals("갤럭시", result.get(1).productName());
        Assertions.assertEquals(3L, result.get(1).score());

        // 검증: ZSET 리빌드 & TTL 부여
        verify(zset).clear();
        verify(zset).addScore(101L, 5.0);
        verify(zset).addScore(202L, 3.0);
        verify(rkeys).expire(eq("product:popular:all:cache"), anyLong(), any());

        // 검증: 이름은 해시에서만 읽었고, DB 이름조회는 호출 안 됨
        verify(productRepo, never()).findNameByProductId(anyList());

        // 추가 안전 체크: 해시 getAll 파라미터 정확성
        verify(nameHash).getAll(eq(expectedFields));
        verifyNoMoreInteractions(nameHash, zset, rkeys, popularRepo, productRepo, redisson);

        // 형식 확인
        Assertions.assertTrue(result.stream().allMatch(dto -> dto.productId() != null && dto.productName() != null));
    }
    @Test
    @DisplayName("캐시 없음 → DB 집계 → ZSET 리빌드 → 이름 해시 비어있음 → DB 조회 성공")
    void cacheMiss_thenDbAndRebuild_thenNameHashEmpty_thenQueryDb_thenSuccess() {
        when(redisson.<Long>getScoredSortedSet("product:popular:all:cache")).thenReturn(zset);
        when(redisson.getKeys()).thenReturn(rkeys);
        when(redisson.<String, String>getMap("product:name")).thenReturn(nameHash);

        when(rkeys.countExists("product:popular:all:cache")).thenReturn(0L);
        var row1 = new PopularAggRow(101L, 5L);
        var row2 = new PopularAggRow(202L, 3L);
        when(popularRepo.findAllTimeTop(10)).thenReturn(List.of(row1, row2));

        when(zset.entryRangeReversed(0, 9)).thenReturn(List.of(
                new ScoredEntry<>(5.0, 101L),
                new ScoredEntry<>(3.0, 202L)
        ));

        //  이름 해시에 값 없음
        var fields = new java.util.HashSet<>(java.util.List.of("101", "202"));
        when(nameHash.getAll(eq(fields))).thenReturn(java.util.Map.of());

        //  DB에서 이름 조회
        when(productRepo.findNameByProductId(List.of(101L, 202L)))
                .thenReturn(java.util.Map.of(
                        101L, "아이폰",
                        202L, "갤럭시"
                ));

        var result = sut.getTopAll(10);

        org.junit.jupiter.api.Assertions.assertEquals(2, result.size());
        org.junit.jupiter.api.Assertions.assertEquals("아이폰", result.get(0).productName());
        org.junit.jupiter.api.Assertions.assertEquals("갤럭시", result.get(1).productName());

        verify(productRepo).findNameByProductId(List.of(101L, 202L)); // ✅ DB 조회 반드시 수행
    }
}