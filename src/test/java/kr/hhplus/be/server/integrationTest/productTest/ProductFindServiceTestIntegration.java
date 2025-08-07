package kr.hhplus.be.server.integrationTest.productTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.ProductFindService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ProductFindServiceTestIntegration {


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductFindService productFindService;

    @Test
    @DisplayName("제품 ID로 제품을 정상 조회할 수 있다")
    public void findProducts() {
        // given - 테스트용 데이터 저장
        ProductJPA savedProduct = productRepository.save(new ProductJPA(
                null,
                "MacBook Pro",
                3000000L,
                10,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        // when - 실제 서비스 호출
        ProductJPA result = productFindService.findProducts(savedProduct.getProductId());

        // then - 검증
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(savedProduct.getProductId());
        assertThat(result.getName()).isEqualTo("MacBook Pro");
        assertThat(result.getPrice()).isEqualTo(3000000L);
    }

    @Test
    @DisplayName("존재하지 않는 제품 ID로 조회하면 예외 발생")
    public void getProductById_fail() {
        Long invalidId = 99999L;

        assertThrows(IllegalArgumentException.class, () -> {
            productFindService.findProducts(invalidId);
        });
    }
}
