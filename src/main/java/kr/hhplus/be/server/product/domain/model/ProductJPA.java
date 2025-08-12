package kr.hhplus.be.server.product.domain.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String name;

    private Long price;

    private int quantity;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void decreaseQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감 수량은 1 이상이어야 합니다.");
        }
        if (this.quantity < quantity) {
            throw new IllegalStateException("재고 부족: 요청=" + quantity + ", 보유=" + this.quantity);
        }
        this.quantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean stockCheck(int requestQuantity) {
        return this.quantity >= requestQuantity;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
