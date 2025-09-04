package kr.hhplus.be.server.couponRedis.domain.model;

import jakarta.persistence.*;
import kr.hhplus.be.server.couponRedis.domain.service.dto.CouponUser;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_coupon", columnNames = {"user_id", "coupon_id"}),
                @UniqueConstraint(name = "uk_coupon_req", columnNames = {"coupon_id", "req_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class CouponUserJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "req_id", length = 64)
    private String reqId; // 멱등키

    private Boolean isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime issuedAt;

    public CouponUser toDomain() {
        return new CouponUser(
                userId,
                couponId,
                reqId,
                isUsed,
                usedAt,
                issuedAt
        );
    }

    public static CouponUserJPA from(CouponUser domain) {
        return CouponUserJPA.builder()
                .userId(domain.getUserId())
                .couponId(domain.getCouponId())
                .reqId(domain.getReqId())
                .isUsed(domain.isUsed())
                .usedAt(domain.getUsedAt())
                .issuedAt(domain.getIssuedAt())
                .build();
    }

    @PrePersist
    public void prePersist() {
        if (issuedAt == null) {
            this.issuedAt = LocalDateTime.now();
        }
    }

}
