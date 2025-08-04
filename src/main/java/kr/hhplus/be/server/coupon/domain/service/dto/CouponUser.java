package kr.hhplus.be.server.coupon.domain.service.dto;

import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CouponUser {
    // Getter only
    private final Long userId;
    private final Long couponId;
    private final boolean isUsed;
    private final LocalDateTime issuedAt;
    private final LocalDateTime usedAt;

    // CouponUser.java (도메인)
    public static CouponUser fromEntity(CouponUserJPA entity) {
        return new CouponUser(
                entity.getUserId(),
                entity.getCouponId(),
                entity.getIsUsed(),
                entity.getIssuedAt(),
                entity.getUsedAt()
        );
    }

    // 도메인 → JPA Entity 변환
    public CouponUserJPA toEntity() {
        return CouponUserJPA.builder()
                .userId(this.userId)
                .couponId(this.couponId)
                .isUsed(this.isUsed)
                .issuedAt(this.issuedAt)
                .usedAt(this.usedAt)
                .build();
    }

    public static CouponUser issue(Long userId, Long couponId) {
        return new CouponUser(userId, couponId, false, LocalDateTime.now(), null);
    }

    public CouponUser markAsUsed() {
        return new CouponUser(userId, couponId, true, issuedAt, LocalDateTime.now());
    }



}
