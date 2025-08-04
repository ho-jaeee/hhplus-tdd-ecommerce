package kr.hhplus.be.server.integrationTest.productTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.repository.ProductHistoryRepository;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ProductHistoryServiceTestInte {

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Autowired
    private ProductHistoryService productHistoryService;

    @Test
    @DisplayName("상품 히스토리를 실제 DB에 저장한다")
    void saveHistory_success() {
        // given
        long productId = 1L;
        long orderId = 100L;
        ProductHistoryJPA.ChangeType changeType = ProductHistoryJPA.ChangeType.SALE;
        int quantity = 5;
        String productName = "맥북";
        long pricePerUnit = 1500000L;

        // when
        productHistoryService.insertHistory(
                productId,
                orderId,
                changeType,
                quantity,
                productName,
                pricePerUnit
        );

        // then
        // DB에서 저장된 데이터 직접 조회하여 검증
        ProductHistoryJPA savedHistory = productHistoryRepository.findAll().stream()
                .filter(h -> h.getProductId() == productId && h.getOrderId() == orderId)
                .findFirst()
                .orElseThrow();

        assertThat(savedHistory.getProductId()).isEqualTo(productId);
        assertThat(savedHistory.getOrderId()).isEqualTo(orderId);
        assertThat(savedHistory.getChangeType()).isEqualTo(changeType);
        assertThat(savedHistory.getQuantity()).isEqualTo(quantity);
        assertThat(savedHistory.getProductName()).isEqualTo(productName);
        assertThat(savedHistory.getPricePerUnit()).isEqualTo(pricePerUnit);
        assertThat(savedHistory.getCreatedAt()).isNotNull();
    }
}
