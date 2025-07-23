package kr.hhplus.be.server.order.domain.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // 예약어 'order' 피함
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId; //주문 ID

    private Long userId; //주문한 user ID

    private Long couponId; // nullable 쿠폰 ID

    private Long totalPrice; //할인 전 금액

    private Long discountedTotalPrice; //최종결제 금액

    @Enumerated(EnumType.STRING) // CREATED, PAID, FAILED, CANCELED
    private OrderStatus status;

    private LocalDateTime createdAt; //주문생성일

    private LocalDateTime updatedAt; //주문변경일

    public enum OrderStatus {
        CREATED, PAID, FAILED, CANCELED
    }


    @PrePersist
    public void prePersist() { // save 초기셋팅
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (status == null) status = OrderStatus.CREATED;
    }

    @PreUpdate
    public void preUpdate() { // update 초기셋팅
        this.updatedAt = LocalDateTime.now();
    }
}

