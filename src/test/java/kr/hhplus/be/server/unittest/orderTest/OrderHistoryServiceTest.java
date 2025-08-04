package kr.hhplus.be.server.unittest.orderTest;


import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.domain.repository.OrderHistoryRepository;

import kr.hhplus.be.server.order.domain.service.OrderHistoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderHistoryServiceTest {

    @Mock
    OrderHistoryRepository orderHistoryRepository;

    @InjectMocks
    OrderHistoryServiceImpl orderHistoryServiceImpl;

    @Test
    @DisplayName("주문 상태와 사유를 포함해 이력을 저장한다")
    void saveOrderHistory() {
        // Given
        Order order = Order.builder()
                .orderId(10001L)
                .userId(1L)
                .couponId(10L)
                .totalPrice(15000L)
                .discountedTotalPrice(12000L)
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.of(2025, 7, 23, 10, 0))
                .updatedAt(LocalDateTime.of(2025, 7, 23, 10, 30))
                .build();

        String reason = "결제 완료";

        ArgumentCaptor<OrderHistoryJPA> captor = ArgumentCaptor.forClass(OrderHistoryJPA.class);

        // When
        orderHistoryServiceImpl.orderInsert(order, reason);

        // Then
        verify(orderHistoryRepository, times(1)).save(captor.capture());

        OrderHistoryJPA saved = captor.getValue();

        assertThat(saved.getOrderId()).isEqualTo(10001L);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(saved.getReason()).isEqualTo("결제 완료");
    }
}
