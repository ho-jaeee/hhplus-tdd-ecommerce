package kr.hhplus.be.server.coupon.domain.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.coupon.domain.service.dto.Coupon;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class CouponJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId; //쿠폰 ID

    private String name; //쿠폰이름

    private int discountAmount; //할인 비율

    private int totalQuantity; //총 수량

    private int issuedQuantity; // 발급 수량

    private LocalDateTime startAt; //쿠폰 시작일자

    private LocalDateTime endAt; //쿠폰 만료일자

    private LocalDateTime createdAt; //쿠폰 생성일시

    private LocalDateTime updatedAt; //쿠폰 수정일시

    public Coupon toDomain() {
        return new Coupon(
                couponId,
                name,
                discountAmount,
                totalQuantity,
                issuedQuantity,
                startAt,
                endAt,
                createdAt,
                updatedAt
        );
    }

    public static CouponJPA from(Coupon domain) {
        return CouponJPA.builder()
                .couponId(domain.getCouponId())
                .name(domain.getName())
                .discountAmount(domain.getDiscountAmount())
                .totalQuantity(domain.getTotalQuantity())
                .issuedQuantity(domain.getIssuedQuantity())
                .startAt(domain.getStartAt())
                .endAt(domain.getEndAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
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
