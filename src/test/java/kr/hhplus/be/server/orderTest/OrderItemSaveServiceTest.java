package kr.hhplus.be.server.orderTest;

import kr.hhplus.be.server.order.domain.model.OrderItem;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.OrderItemSaveServiceImpl;
import kr.hhplus.be.server.order.domain.service.OrderSaveServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderItemSaveServiceTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemSaveServiceImpl orderItemSaveService;


    @Test
    @DisplayName("주문Item 정보를 저장한다")
    void saveOrderItem() {
        // Given
        Long orderId = 1L;
        OrderItem item1 = OrderItem.create(orderId,101L, "상품1", 2, 5000);
        OrderItem item2 = OrderItem.create(orderId, 102L, "상품2", 1, 10000);
        List<OrderItem> items = List.of(item1, item2);

        // Mock repository insert behavior
        OrderItemJPA entity1 = item1.toEntity(orderId);
        OrderItemJPA entity2 = item2.toEntity(orderId);

        given(orderItemRepository.insert(any(OrderItemJPA.class)))
                .willReturn(entity1, entity2);

        // When
        List<OrderItemJPA> result = orderItemSaveService.itemSave(orderId, items);

        // Then
        assertThat(result).hasSize(2);
        verify(orderItemRepository, times(2)).insert(any(OrderItemJPA.class));

        assertThat(result.get(0).getProductId()).isEqualTo(101L);
        assertThat(result.get(1).getProductId()).isEqualTo(102L);

    }
}
