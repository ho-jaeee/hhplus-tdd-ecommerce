package kr.hhplus.be.server.coupon.domain.model;

import jakarta.persistence.*;
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

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isValidPeriod() { // 만료일자 계산
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt);
    }

    public void increaseIssuedQuantity() { //발급 시 수량증가
        this.issuedQuantity++;
    }
}
