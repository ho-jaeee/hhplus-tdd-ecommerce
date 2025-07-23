package kr.hhplus.be.server.product.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductHistoryJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    private int quantity;

    private String productName;

    private Long pricePerUnit;

    private LocalDateTime createdAt;

    public enum ChangeType {
        SALE, RESTOCK, RETURN, MANUAL
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
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
