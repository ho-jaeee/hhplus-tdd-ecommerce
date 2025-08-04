package kr.hhplus.be.server.integrationTest.productTest;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.ProductDecreaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ProductDecreaseServiceTestInte {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductDecreaseService productDecreaseService;

    @Test
    @DisplayName("재고를 실제 DB에서 차감한다.")
    void decreaseStock_success() {
        // given - 실제 DB에 저장
        ProductJPA savedProduct = productRepository.save(new ProductJPA(
                null, "상품", 5000L, 10, LocalDateTime.now(), LocalDateTime.now()
        ));

        // when
        productDecreaseService.decreaseStock(savedProduct.getProductId(), 4);

        // then - 변경사항 확인
        ProductJPA updatedProduct = productRepository.findById(savedProduct.getProductId()).orElseThrow();
        assertThat(updatedProduct.getQuantity()).isEqualTo(6);
    }

}
