package kr.hhplus.be.server.productTest;

import kr.hhplus.be.server.productTest.domain.model.ProductJPA;
import kr.hhplus.be.server.productTest.domain.repository.ProductRepository;
import kr.hhplus.be.server.productTest.domain.service.ProductCheckService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class ProductStockCheckTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductCheckService productCheckService;


        @Test
        @DisplayName("재고가 충분하면 예외 없이 통과한다")  // True test
        void validateStock_success() {
            // given
            ProductJPA product = new ProductJPA(
                    1L, "상품", 1000L, 10,
                    LocalDateTime.now(), LocalDateTime.now()
            );
            given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));

            // when & then
            assertThatCode(() -> productCheckService.stockCheck(1L, 5))
                    .doesNotThrowAnyException();
        }

    @Test
    @DisplayName("재고가 부족하면 예외를 던진다") // False test
    void validateStock_insufficient() {
        // given
        ProductJPA product = new ProductJPA(
                1L, "상품", 1000L, 2,
                LocalDateTime.now(), LocalDateTime.now()
        );
        given(productRepository.findByProductId(1L)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productCheckService.stockCheck(1L, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고 부족");
    }


    @Test
    @DisplayName("상품이 존재하지 않으면 예외를 던진다")
    void validateStock_productNotFound() {
        // given
        given(productRepository.findByProductId(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productCheckService.stockCheck(1L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }


}
