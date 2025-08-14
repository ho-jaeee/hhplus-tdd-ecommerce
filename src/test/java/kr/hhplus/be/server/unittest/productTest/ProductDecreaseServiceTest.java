package kr.hhplus.be.server.unittest.productTest;


import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.ProductDecreaseService;
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

@ExtendWith(MockitoExtension.class)
public class ProductDecreaseServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductDecreaseService productDecreaseService;

    @Test
    @DisplayName("재고를 차감한다.")
    void decreaseStock_success() {
        // given
        ProductJPA product = new ProductJPA(1L, "상품", 5000L, 10, LocalDateTime.now(), LocalDateTime.now());
        given(productRepository.findById(1L)).willReturn(Optional.of(product));

        // when
        productDecreaseService.decreaseStock(1L, 4);

        // then
        assertThat(product.getQuantity()).isEqualTo(6);
    }

}
