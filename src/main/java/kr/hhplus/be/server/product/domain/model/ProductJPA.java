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
    @Column(name = "product_id")
    private Long productId;

    private String name;

    private Long price;

    private int quantity;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public void decreaseQuantity(int amount) {
//        if (this.quantity < amount) {
//            throw new IllegalStateException("재고 부족");
//        }
        this.quantity -= amount;
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
