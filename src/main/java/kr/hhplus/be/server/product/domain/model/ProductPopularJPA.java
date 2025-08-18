package kr.hhplus.be.server.product.domain.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_popular",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_product_bucket", columnNames = {"productId", "bucket"})
        }
)
@Getter
@Setter
public class ProductPopularJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**제품 ID **/
    private Long productId;
    /** 누적 판매량 **/
    private Long score;
   /** 시 단위 버킷 시작 (예: 2025-08-14T11:00:00) */
    @Column(nullable = false)
    private LocalDateTime bucketStart;

    private LocalDateTime updatedAt;
}

