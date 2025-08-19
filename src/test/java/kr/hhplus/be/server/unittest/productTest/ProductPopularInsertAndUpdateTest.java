package kr.hhplus.be.server.unittest.productTest;


import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.domain.service.ProductPopularInsertAndUpdateServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductPopularInsertAndUpdateTest {

    @Mock
    ProductPopularRepository popularRepository;

    @InjectMocks
    ProductPopularInsertAndUpdateServiceImpl service;

    @Test
    @DisplayName("productId + buckerStart 값이 없음 신규생성 Insert")
    void addSaleInsertTest() {
        // given
        long productId = 100L;
        long quantity = 3L;
        LocalDateTime ts = LocalDateTime.of(2025, 8, 18, 13, 25, 47); // 임의 시각
        LocalDateTime bucket = ts.withMinute(0).withSecond(0).withNano(0); // 13:00 버킷

        when(popularRepository.findByProductIdAndBucketStart(productId, bucket))
                .thenReturn(Optional.empty());

        ArgumentCaptor<ProductPopularJPA> captor = ArgumentCaptor.forClass(ProductPopularJPA.class);
        when(popularRepository.save(captor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        service.addSale(productId, quantity, ts);

        // then
        ProductPopularJPA saved = captor.getValue();
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getBucketStart()).isEqualTo(bucket);
        assertThat(saved.getScore()).isEqualTo(quantity); // 0 + quantity
        verify(popularRepository).findByProductIdAndBucketStart(productId, bucket);
        verify(popularRepository).save(any(ProductPopularJPA.class));
        verifyNoMoreInteractions(popularRepository);
    }

    @Test
    @DisplayName("productId + buckerStart 값이 있음 갱신 Update")
    void addSale_updates_when_exists() {
        // given
        long productId = 100L;
        long quantity = 5L;
        LocalDateTime ts = LocalDateTime.of(2025, 8, 18, 13, 40, 0);
        LocalDateTime bucket = ts.withMinute(0).withSecond(0).withNano(0);

        ProductPopularJPA exists = new ProductPopularJPA();
        exists.setId(1L);
        exists.setProductId(productId);
        exists.setBucketStart(bucket);
        exists.setScore(7L); // 기존 누적값

        when(popularRepository.findByProductIdAndBucketStart(productId, bucket))
                .thenReturn(Optional.of(exists));

        ArgumentCaptor<ProductPopularJPA> captor = ArgumentCaptor.forClass(ProductPopularJPA.class);
        when(popularRepository.save(captor.capture()))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        service.addSale(productId, quantity, ts);

        // then
        ProductPopularJPA saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getBucketStart()).isEqualTo(bucket);
        assertThat(saved.getScore()).isEqualTo(12L); // 7 + 5
        verify(popularRepository).findByProductIdAndBucketStart(productId, bucket);
        verify(popularRepository).save(any(ProductPopularJPA.class));
        verifyNoMoreInteractions(popularRepository);
    }

}
