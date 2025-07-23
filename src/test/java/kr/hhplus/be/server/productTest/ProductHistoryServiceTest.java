package kr.hhplus.be.server.productTest;


import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.repository.ProductHistoryRepository;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductHistoryServiceTest {

    @Mock
    ProductHistoryRepository productHistoryRepository;

    @InjectMocks
    ProductHistoryService productHistoryService;

    @Test
    @DisplayName("상품 히스토리를 입력한다")
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
        ArgumentCaptor<ProductHistoryJPA> captor = ArgumentCaptor.forClass(ProductHistoryJPA.class);
        verify(productHistoryRepository).insert(captor.capture());


        ProductHistoryJPA inserted = captor.getValue();

        assertThat(inserted.getProductId()).isEqualTo(productId);
        assertThat(inserted.getOrderId()).isEqualTo(orderId);
        assertThat(inserted.getChangeType()).isEqualTo(changeType);
        assertThat(inserted.getQuantity()).isEqualTo(quantity);
        assertThat(inserted.getProductName()).isEqualTo(productName);
        assertThat(inserted.getPricePerUnit()).isEqualTo(pricePerUnit);
        assertThat(inserted.getCreatedAt()).isNotNull();  // 생성 시점 확인



    }

}
