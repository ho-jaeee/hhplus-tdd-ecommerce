package kr.hhplus.be.server.order.pollicy;

import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;

import java.util.List;

public class OrderPriceCalculator {

    private OrderPriceCalculator() {
        // 유틸 클래스이므로 인스턴스 생성 방지
    }

    public static long calculateTotalPrice(List<OrderItemCommand> items) {
        if (items == null || items.isEmpty()) return 0L;

        return items.stream()
                .mapToLong(item -> item.pricePerUnit() * item.quantity())
                .sum();
    }
}
