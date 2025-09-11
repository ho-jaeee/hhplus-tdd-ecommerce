package kr.hhplus.be.server.couponRedis.domain.service.dto;


import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;



@Getter
@AllArgsConstructor
public class Coupon {
    // Getter only
    private final Long CouponId;
    private final String name;
    private final int discountAmount;
    private final int totalQuantity;
    private final int issuedQuantity;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    // JPA Entity -> 도메인 객체로 변환
    public static Coupon fromEntity(CouponJPA entity) {
        return new Coupon(
                entity.getCouponId(),
                entity.getName(),
                entity.getDiscountAmount(),
                entity.getTotalQuantity(),
                entity.getIssuedQuantity(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CouponJPA toEntity() {
        return CouponJPA.builder()
                .couponId(CouponId)
                .name(name)
                .discountAmount(discountAmount)
                .totalQuantity(totalQuantity)
                .issuedQuantity(issuedQuantity)
                .startAt(startAt)
                .endAt(endAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // 비즈니스 행위
    public boolean isValidPeriod() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startAt) && now.isBefore(endAt);
    }

    public boolean canIssueMore() {
        return issuedQuantity < totalQuantity;
    }

    public Coupon increaseIssuedQuantity() {
        return new Coupon(
                CouponId, name, discountAmount, totalQuantity, issuedQuantity + 1,
                startAt, endAt, createdAt, LocalDateTime.now()
        );
    }

}
