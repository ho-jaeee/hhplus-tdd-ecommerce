package kr.hhplus.be.server.integrationTest.eventTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import kr.hhplus.be.server.product.domain.service.ProductPopularSaveService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class HistoryEventHandlersIntegrationTest {


    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager txManager;

    @MockBean OrderHistoryService orderHistoryService;
    @MockBean ProductHistoryService productHistoryService;
    @MockBean ProductPopularSaveService productPopularSaveService;

    @Test
    @DisplayName("AFTER_COMMIT: 주문 이력 1회 + 상품 이력 아이템별 1회 + DB 집계 아이템별 1회 호출")
    void afterCommit_allHandlers_invoked_perSpec() {
        // given
        LocalDateTime now = LocalDateTime.now();

        // 아이템 2개 (productId, name, unitPrice, qty)
        OrderItemCommand i1 = new OrderItemCommand(10L, "A", 1_000L, 2);
        OrderItemCommand i2 = new OrderItemCommand(20L, "B", 2_000L, 3);
        List<OrderItemCommand> items = List.of(i1, i2);

        Order order = Order.create(
                /*userId*/   10001L,
                /*couponId*/ null,
                /*total*/    1_000L * 2 + 2_000L * 3,
                /*payPoint*/ 10_000L,
                /*status*/   OrderStatus.PAID,
                /*created*/  now,
                /*updated*/  now
        );
        long payPoint = 10_000L;

        OrderPlacedEvent event = OrderPlacedEvent.of(items, order, payPoint);

        // when: 트랜잭션 내 발행 → 커밋 시 AFTER_COMMIT 실행
        new TransactionTemplate(txManager).executeWithoutResult(s -> publisher.publishEvent(event));

        // then --- 1) 주문 이력 1회
        verify(orderHistoryService, times(1))
                .orderInsert(eq(order), eq("결제완료"));

        // then --- 2) 상품 이력: 각 아이템별 1회
        verify(productHistoryService, times(1))
                .insertHistory(eq(10L), eq(order.getOrderId()),
                        eq(ProductHistoryJPA.ChangeType.SALE),
                        eq(2), eq("A"), eq(1_000L));

        verify(productHistoryService, times(1))
                .insertHistory(eq(20L), eq(order.getOrderId()),
                        eq(ProductHistoryJPA.ChangeType.SALE),
                        eq(3), eq("B"), eq(2_000L));

        // then --- 3) DB 집계: 각 아이템별 1회 (발생 시각은 any(LocalDateTime))
        verify(productPopularSaveService, times(1))
                .addSale(eq(10L), eq(2L), ArgumentMatchers.any(LocalDateTime.class));
        verify(productPopularSaveService, times(1))
                .addSale(eq(20L), eq(3L), ArgumentMatchers.any(LocalDateTime.class));

        verifyNoMoreInteractions(orderHistoryService, productHistoryService, productPopularSaveService);
    }

}
