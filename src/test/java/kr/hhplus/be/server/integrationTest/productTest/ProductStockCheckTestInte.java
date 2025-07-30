package kr.hhplus.be.server.integrationTest.productTest;


import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")  // 테스트용 설정 프로파일 지정 (application-test.yml 등)
@Transactional  // 테스트 완료 후 DB 롤백
public class ProductStockCheckTestInte {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductCheckService productCheckService;

    @Test
    @DisplayName("재고가 충분하면 예외 없이 통과한다")
    void validateStock_success() {
        // given - 실제 DB에 데이터 저장
        ProductJPA product = productRepository.save(new ProductJPA(
                null, "상품", 1000L, 10,
                LocalDateTime.now(), LocalDateTime.now()
        ));

        // when & then
        assertThatCode(() -> productCheckService.stockCheck(product.getProductId(), 5))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("재고가 부족하면 예외를 던진다")
    void validateStock_insufficient() {
        // given
        ProductJPA product = productRepository.save(new ProductJPA(
                null, "상품", 1000L, 2,
                LocalDateTime.now(), LocalDateTime.now()
        ));

        // when & then
        assertThatThrownBy(() -> productCheckService.stockCheck(product.getProductId(), 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("재고 부족");
    }

    @Test
    @DisplayName("상품이 존재하지 않으면 예외를 던진다")
    void validateStock_productNotFound() {
        // 존재하지 않는 ID 사용 (예: 99999L)
        assertThatThrownBy(() -> productCheckService.stockCheck(99999L, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품을 찾을 수 없습니다.");
    }
}
