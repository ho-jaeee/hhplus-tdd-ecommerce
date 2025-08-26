package kr.hhplus.be.server.order.domain.model;


import jakarta.persistence.*;
import kr.hhplus.be.server.order.domain.service.dto.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "order_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_history",
                columnNames = {"order_id", "status_text"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderHistoryJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING) // 상태표현만 하기 때문에 SRP 위배라고 생각안함
    @Column(name = "status_text", nullable = false)
    private OrderStatus status;

    private String reason;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;



    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) this.changedAt = LocalDateTime.now();

    }
}
