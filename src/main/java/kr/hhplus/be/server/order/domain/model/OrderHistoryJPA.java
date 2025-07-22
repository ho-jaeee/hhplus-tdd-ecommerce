package kr.hhplus.be.server.order.domain.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderHistoryJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    @Enumerated(EnumType.STRING) // 상태표현만 하기 때문에 SRP 위배라고 생각안함
    private OrderJPA.OrderStatus status;

    private String reason;

    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        this.changedAt = LocalDateTime.now();
    }
}
