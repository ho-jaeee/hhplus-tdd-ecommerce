package kr.hhplus.be.server.unittest.orderTest;


import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.order.domain.service.OrderSaveServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderSaveServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderSaveServiceImpl orderSaveServiceImpl;

    @Test
    @DisplayName("주문 정보를 저장한다")
    void saveOrder() {
        // given
        Order order = new Order(
                null,
                1L,
                10L,
                15000L,
                12000L,
                OrderStatus.PAID,
                LocalDateTime.of(2025, 7, 29, 10, 0),
                LocalDateTime.of(2025, 7, 29, 10, 30)
        );

        OrderJPA savedEntity = OrderJPA.builder()
                .orderId(10001L)
                .userId(1L)
                .couponId(10L)
                .totalPrice(15000L)
                .discountedTotalPrice(12000L)
                .status(OrderStatus.PAID)
                .build();


        when(orderRepository.save(any(OrderJPA.class))).thenReturn(savedEntity);

        // when
        Order saved = orderSaveServiceImpl.save(order);

        // then
        ArgumentCaptor<OrderJPA> captor = ArgumentCaptor.forClass(OrderJPA.class);
        verify(orderRepository, times(1)).save(captor.capture());

        OrderJPA captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo(1L);
        assertThat(saved.getOrderId()).isEqualTo(10001L);
        assertThat(saved.getTotalPrice()).isEqualTo(15000L);
    }

}
