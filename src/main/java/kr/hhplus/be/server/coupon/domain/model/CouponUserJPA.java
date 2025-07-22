package kr.hhplus.be.server.coupon.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class CouponUserJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long couponId;

    private boolean isUsed;

    private LocalDateTime usedAt;

    private LocalDateTime issuedAt;

    public void useCoupon() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (issuedAt == null) {
            this.issuedAt = LocalDateTime.now();
        }
    }

}
