package kr.hhplus.be.server.couponRedisKafka.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "coupon_user_new",
        uniqueConstraints = @UniqueConstraint(name = "uq_coupon_user_idem", columnNames = "idem_key")
)
@Getter
@Setter
public class CouponUserNewJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="coupon_id", nullable=false)
    private Long couponId;      // (간단히 Long로 보관; 연관관계 매핑 원하면 @ManyToOne로 변경)

    @Column(name="user_id", nullable=false)
    private Long userId; // 사용자 Id

    @Column(name="idem_key", nullable=false, length=128)
    private String idemKey;     // 멱등키 UNIQUE

    @Column(name="created_at", nullable=false)
    private Instant createdAt; //발급일시

}
