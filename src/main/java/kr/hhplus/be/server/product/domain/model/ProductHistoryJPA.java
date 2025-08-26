package kr.hhplus.be.server.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_product_history",
                columnNames = {"order_id", "product_id", "change_type"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductHistoryJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "price_per_unit")
    private Long pricePerUnit;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum ChangeType {
        SALE, RESTOCK, RETURN, MANUAL
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public static ProductHistoryJPA createWithoutId(
            Long productId,
            Long orderId,
            ChangeType changeType,
            int quantity,
            String productName,
            Long pricePerUnit
    ) {
        return new ProductHistoryJPA(
                null,                  // id는 JPA에서 자동 생성
                productId,
                orderId,
                changeType,
                quantity,
                productName,
                pricePerUnit,
                LocalDateTime.now()
        );
    }
}
