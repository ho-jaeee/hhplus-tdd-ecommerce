package kr.hhplus.be.server.order.domain.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "order_event"
)
@Getter @Setter
public class OrderKafkaJPA {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "total_price", nullable = false)
    private long totalPrice;

    @Column(name = "pay_point", nullable = false)
    private long payPoint;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "orderEvent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderKafkaItemJPA> items = new ArrayList<>();

    // 양방향 편의 메서드
    public void addItem(OrderKafkaItemJPA item) {
        item.setOrderEvent(this);
        this.items.add(item);
    }

    public void removeItem(OrderKafkaItemJPA item) {
        item.setOrderEvent(null);
        this.items.remove(item);
    }
}
