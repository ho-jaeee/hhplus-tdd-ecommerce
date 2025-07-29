package kr.hhplus.be.server.order.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class OrderHistory {

    private final Long orderId;
    private final OrderStatus status;
    private final String reason;
    private final LocalDateTime changedAt;


    public OrderHistory(Long orderId, OrderStatus status, String reason) {
        this.orderId = orderId;
        this.status = status;
        this.reason = reason;
        this.changedAt = LocalDateTime.now();
    }

    // JPA로 변환하는 메서드
    public OrderHistoryJPA toEntity() {
        return OrderHistoryJPA.builder()
                .orderId(orderId)
                .status(status)
                .reason(reason)
                .changedAt(changedAt)
                .build();
    }


}
