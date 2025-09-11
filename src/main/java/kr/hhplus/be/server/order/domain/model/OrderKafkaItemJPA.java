package kr.hhplus.be.server.order.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "order_event_item",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_item", columnNames = {"order_id", "product_id"})
        }
)
@Getter @Setter
public class OrderKafkaItemJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT) // ★ FK 제약 생성 금지
    )
    private OrderKafkaJPA orderEvent;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "price_per_unit", nullable = false)
    private long pricePerUnit;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
