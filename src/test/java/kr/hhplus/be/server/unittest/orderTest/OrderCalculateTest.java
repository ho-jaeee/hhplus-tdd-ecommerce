package kr.hhplus.be.server.unittest.orderTest;

import kr.hhplus.be.server.order.pollicy.OrderPriceCalculator;
import kr.hhplus.be.server.order.usecase.OrderItemCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderCalculateTest {

        @Test
        @DisplayName("여러 상품의 총액이 정확히 계산된다")
        void totalPrice_should_be_calculated_correctly () {
            List<OrderItemCommand> items = List.of(
                    new OrderItemCommand(1L, "상품1", 10000L, 2),  // 20000
                    new OrderItemCommand(2L, "상품2", 5000L, 3)   // 15000
            );

            long total = OrderPriceCalculator.calculateTotalPrice(items);

            assertThat(total).isEqualTo(35000L);
        }

        @Test
        @DisplayName("빈 리스트의 총액은 0이다")
        void totalPrice_should_be_zero_when_empty () {
            long total = OrderPriceCalculator.calculateTotalPrice(List.of());
            assertThat(total).isZero();
        }

        @Test
        @DisplayName("수량이 0인 항목은 총액에 영향을 주지 않는다")
        void totalPrice_should_ignore_zero_quantity () {
            List<OrderItemCommand> items = List.of(
                    new OrderItemCommand(1L, "상품1", 10000L, 0),
                    new OrderItemCommand(2L, "상품2", 3000L, 2)
            );

            long total = OrderPriceCalculator.calculateTotalPrice(items);

            assertThat(total).isEqualTo(6000L);
        }

        @Test
        @DisplayName("하나의 상품만 있어도 총액이 계산된다")
        void totalPrice_with_single_item () {
            List<OrderItemCommand> items = List.of(
                    new OrderItemCommand(1L, "상품1", 12345L, 1)
            );

            long total = OrderPriceCalculator.calculateTotalPrice(items);

            assertThat(total).isEqualTo(12345L);
        }
}

