package kr.hhplus.be.server.order.domain.service.dto;

import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
public class Order{

    private final Long orderId;
    private final Long userId;
    private final Long couponId;
    private final Long totalPrice;
    private final Long discountedTotalPrice;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Order(Long orderId,
                 Long userId,
                 Long couponId,
                 Long totalPrice,
                 Long discountedTotalPrice,
                 OrderStatus status,
                 LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.couponId = couponId;
        this.totalPrice = totalPrice;
        this.discountedTotalPrice = discountedTotalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Order create(Long userId, Long couponId, long totalPrice, long discountedTotalPrice, OrderStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Order(null, userId, couponId, totalPrice, discountedTotalPrice, OrderStatus.PAID, createdAt, updatedAt);
    }



    public Order withId(Long orderId) {
        return new Order(orderId, userId, couponId, totalPrice, discountedTotalPrice, status, createdAt, updatedAt);
    }

    public OrderJPA toEntity() {
        return OrderJPA.builder()
                .userId(userId)
                .couponId(couponId)
                .totalPrice(totalPrice)
                .discountedTotalPrice(discountedTotalPrice)
                .status(status)
                .build();
    }

    public OrderHistoryJPA toHistory(String reason) {
        return OrderHistoryJPA.builder()
                .orderId(this.orderId)
                .status(this.status)
                .reason(reason)
                .changedAt(LocalDateTime.now())
                .build();
    }

}
