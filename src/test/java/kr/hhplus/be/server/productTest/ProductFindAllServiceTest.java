package kr.hhplus.be.server.productTest;

import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.ProductFindAllService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductFindAllServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductFindAllService productFindAllService;


    @Test
    @DisplayName("전체 상품 조회 - 성공")
    void findAllProducts() {
        // given
        ProductJPA product1 = new ProductJPA(1L, "상품A", 10000L, 10, null, null);
        ProductJPA product2 = new ProductJPA(2L, "상품B", 20000L, 5, null, null);
        List<ProductJPA> expectedProducts = List.of(product1, product2);

        when(productRepository.findAll()).thenReturn(expectedProducts);

        // when
        List<ProductJPA> result = productFindAllService.findAllProducts();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("상품A");
        assertThat(result.get(1).getPrice()).isEqualTo(20000L);
    }
}
