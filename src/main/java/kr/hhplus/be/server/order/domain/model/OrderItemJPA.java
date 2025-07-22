package kr.hhplus.be.server.order.domain.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItemJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId; //주문 ID

    private Long productId; //상품Id

    private String productName; //상품이름

    private Long pricePerUnit; //단가

    private Integer quantity; //수량

    private Long totalPrice; //최종가격(단가 * 수량)

    private LocalDateTime createdAt;

    @PrePersist //초기셋팅
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.totalPrice = pricePerUnit * quantity;
    }

}
