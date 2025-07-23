package kr.hhplus.be.server.productTest;


import kr.hhplus.be.server.productTest.domain.model.ProductJPA;
import kr.hhplus.be.server.productTest.domain.repository.ProductRepository;

import kr.hhplus.be.server.productTest.domain.service.ProductFindService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductFindServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductFindService productFindService;

    @Test
    @DisplayName("제품 ID로 제품을 정상 조회할 수 있다")
    public void findProducts() {
        // given
        Long productId = 1L;
        ProductJPA mockProduct = new ProductJPA(
                productId,
                "MacBook Pro",
                3000000L,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(mockProduct));

        // when
        ProductJPA result = productFindService.findProducts(productId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("MacBook Pro");
        assertThat(result.getPrice()).isEqualTo(3000000L);

    }

    @Test
    @DisplayName("존재하지 않는 제품 ID로 조회하면 예외 발생")
    void getProductById_fail() {

        // given
        Long productId = 99L;
        given(productRepository.findByProductId(productId)).willReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            productFindService.findProducts(productId);
        });
    }
}
