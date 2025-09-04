package kr.hhplus.be.server.integrationTest.eventTest;



import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.order.component.OrderPlacedEvent;
import kr.hhplus.be.server.order.domain.service.OrderHistoryService;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.product.domain.service.ProductHistoryService;
import kr.hhplus.be.server.product.domain.service.ProductPopularCacheService;
import kr.hhplus.be.server.product.domain.service.ProductPopularSaveService;
import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class ProductSalesCacheAggregationIntegrationTest {

    @Autowired ApplicationEventPublisher publisher;
    @Autowired PlatformTransactionManager txManager;

    // 최종 검증 대상
    @MockBean ProductPopularCacheService productPopularCacheService;

    // 다른 리스너들이 참조하는 서비스는 Mock으로 격리 (예외/부작용 방지)
    @MockBean OrderHistoryService orderHistoryService;
    @MockBean ProductHistoryService productHistoryService;
    @MockBean ProductPopularSaveService productPopularSaveService;

    @Test
    @DisplayName("AFTER_COMMIT 체인: OrderPlacedEvent → (Producer) ProductSalesCacheEvent → (Handler) addSalesBatch 1회 호출 및 payload 검증")
    void endToEnd_cacheBatch_called_once() {
        // given
        LocalDateTime now = LocalDateTime.now();

        var i1 = new OrderItemCommand(10L, "A", 1_000L, 2);
        var i2 = new OrderItemCommand(20L, "B", 2_000L, 3);
        var items = List.of(i1, i2);

        var order = Order.create(
                /*userId*/   10001L,
                /*couponId*/ null,
                /*total*/    1_000L * 2 + 2_000L * 3,
                /*payPoint*/ 10_000L,
                /*status*/   OrderStatus.PAID,
                /*created*/  now,
                /*updated*/  now
        );

        long payPoint = 10_000L;
        var domainEvent = OrderPlacedEvent.of(items, order, payPoint);

        // when: 트랜잭션 내 발행 → 커밋 시 Producer(AFTER_COMMIT)가 전용 이벤트 publish
        new TransactionTemplate(txManager).executeWithoutResult(s -> publisher.publishEvent(domainEvent));

        // then: 캐시 서비스 1회 호출 + DTO/메타 검증
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProductPopularDto>> listCap = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Instant> instantCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<String> idCap = ArgumentCaptor.forClass(String.class);

        verify(productPopularCacheService, times(1))
                .addSalesBatch(listCap.capture(), instantCap.capture(), idCap.capture());

        var dtos = listCap.getValue();
        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(ProductPopularDto::productId)
                .containsExactlyInAnyOrder(10L, 20L);
        assertThat(dtos).extracting(ProductPopularDto::productName)
                .containsExactlyInAnyOrder("A", "B");
        assertThat(dtos).extracting(ProductPopularDto::score)
                .containsExactlyInAnyOrder(2L, 3L); // Long 타입 일치

        // eventId 규칙: 상위에서 eventId를 제공하면 그 값(예: UUID), 아니면 "order:{orderId}"로 fallback
        String eventId = idCap.getValue();
        assertThat(eventId).satisfiesAnyOf(
                v -> assertThat(v).isEqualTo("order:" + order.getOrderId()),
                v -> assertThat(v).matches("^[0-9a-fA-F-]{36}$") // UUID 패턴
        );

        // occurred는 값 존재만 확인
        assertThat(instantCap.getValue()).isNotNull();

        verifyNoMoreInteractions(productPopularCacheService);
    }
}
