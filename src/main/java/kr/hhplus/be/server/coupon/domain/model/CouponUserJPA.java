package kr.hhplus.be.server.coupon.domain.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
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
    private Boolean isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime issuedAt;

    public CouponUser toDomain() {
        return new CouponUser(
                userId,
                couponId,
                isUsed,
                usedAt,
                issuedAt
        );
    }

    public static CouponUserJPA from(CouponUser domain) {
        return CouponUserJPA.builder()
                .userId(domain.getUserId())
                .couponId(domain.getCouponId())
                .isUsed(domain.isUsed())
                .usedAt(domain.getIssuedAt())
                .issuedAt(domain.getUsedAt())
                .build();
    }

    @PrePersist
    public void prePersist() {
        if (issuedAt == null) {
            this.issuedAt = LocalDateTime.now();
        }
    }

}
