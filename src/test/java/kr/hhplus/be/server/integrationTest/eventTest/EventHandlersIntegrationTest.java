package kr.hhplus.be.server.integrationTest.eventTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.ProductPopularSaveService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class EventHandlersIntegrationTest {


    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager txManager;

    // 이력저장 검증용
    @MockBean OrderHistoryService orderHistoryService;
    @MockBean ProductHistoryService productHistoryService;

    // 집계 검증용
    @MockBean ProductPopularSaveService productPopularSaveService;
    @MockBean ProductPopularCacheService productPopularCacheService;

    /**
     * 1) 이력저장 이벤트가 AFTER_COMMIT에서 잘 호출된다.
     *    - 주문 이력(orderInsert) 호출 확인
     *    - (아이템 비우면) 상품 이력(insertHistory)은 호출 안 됨
     */
    @Test
    void 이력저장_이벤트가_AFTER_COMMIT에서_정상_호출된다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.create(
                /*userId*/ 10001L,
                /*couponId*/ null,
                /*totalPrice*/ 10_000L,
                /*payPoint*/ 10_000L,
                /*status*/ OrderStatus.PAID,
                /*createdAt*/ now,
                /*updatedAt*/ now
        );
        List<OrderItemCommand> items = List.of(); // 비워서 상품 이력 호출을 피함
        long payPoint = 10_000L;

        OrderPlacedEvent event = OrderPlacedEvent.of(items, order, payPoint);

        // when: 트랜잭션 내에서 publish → 커밋 시 AFTER_COMMIT 리스너 실행
        new TransactionTemplate(txManager).executeWithoutResult(status -> {
            publisher.publishEvent(event);
        });

        // then
        verify(orderHistoryService, times(1)).orderInsert(eq(order), eq("결제완료"));
        verify(productHistoryService, times(0)).insertHistory(
                anyLong(), anyLong(), eq(ProductHistoryJPA.ChangeType.SALE), (int) anyLong(), anyString(), anyLong()
        );
    }

    /**
     * 2) 상품판매량 집계 이벤트가 AFTER_COMMIT에서 잘 호출된다.
     *    - DB 집계(addSale) 두 번 호출(2개 아이템)
     *    - 캐시 집계(addSalesBatch) 한 번 호출(2개 DTO)
     *    - DB → Cache 순서(InOrder)까지 검증
     */
    @Test
    void 상품판매량_집계_이벤트가_AFTER_COMMIT에서_정상_호출된다() {
        // given: 아이템 2개
        OrderItemCommand i1 = new OrderItemCommand(10L, "A", 1_000L, 2);
        OrderItemCommand i2 = new OrderItemCommand(20L, "B", 2_000L, 3);

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.create(
                10002L, null,
                /*totalPrice*/ 2_000L*3 + 1_000L*2, // 예시
                /*payPoint*/ 10_000L,
                OrderStatus.PAID, now, now
        );
        long payPoint = 10_000L;

        OrderPlacedEvent event = OrderPlacedEvent.of(List.of(i1, i2), order, payPoint);

        // when
        new TransactionTemplate(txManager).executeWithoutResult(status -> {
            publisher.publishEvent(event);
        });

        // then: DB 집계 호출(아이템별 1회씩)
        verify(productPopularSaveService, times(1))
                .addSale(eq(10L), eq(2L), any(LocalDateTime.class));
        verify(productPopularSaveService, times(1))
                .addSale(eq(20L), eq(3L), any(LocalDateTime.class));

        // then: 캐시 집계 호출(한 번), DTO/Instant 캡쳐
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProductPopularDto>> dtosCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Instant> instantCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<String> idCap = ArgumentCaptor.forClass(String.class);

        verify(productPopularCacheService, times(1))
                .addSalesBatch(dtosCap.capture(), instantCap.capture(), idCap.capture());

        var dtos = dtosCap.getValue();
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0)).isEqualTo(new ProductPopularDto(10L, "A", 2L));
        assertThat(dtos.get(1)).isEqualTo(new ProductPopularDto(20L, "B", 3L));

        // occurredAt → Instant 변환이 올바르게 들어오는지(값 자체는 any로 캡쳐했으므로 null 아님만 체크)
        assertThat(instantCap.getValue()).isNotNull();
        // eventId는 e.getEventId()가 null이면 "order:{orderId}"로 대체됨 — 정확한 값은 이벤트 구현에 따르므로 존재만 확인
        assertThat(idCap.getValue()).isNotBlank();

        // (선택) DB → Cache 호출 순서 검증
        InOrder inOrder = inOrder(productPopularSaveService, productPopularCacheService);
        inOrder.verify(productPopularSaveService).addSale(eq(10L), eq(2L), any(LocalDateTime.class));
        inOrder.verify(productPopularSaveService).addSale(eq(20L), eq(3L), any(LocalDateTime.class));
        inOrder.verify(productPopularCacheService).addSalesBatch(anyList(), any(), anyString());
    }

}
