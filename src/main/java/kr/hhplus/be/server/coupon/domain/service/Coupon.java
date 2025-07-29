package kr.hhplus.be.server.coupon.domain.service;


import lombok.Getter;

import java.time.LocalDateTime;



@Getter
public class Coupon {
    // Getter only
    private final Long id;
    private final String name;
    private final int discountAmount;
    private final int totalQuantity;
    private final int issuedQuantity;
    private final LocalDateTime startAt;
    private final LocalDateTime endAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    public Coupon(Long id, String name, int discountAmount, int totalQuantity, int issuedQuantity,
                  LocalDateTime startAt, LocalDateTime endAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.discountAmount = discountAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.startAt = startAt;
        this.endAt = endAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
                id, name, discountAmount, totalQuantity, issuedQuantity + 1,
                startAt, endAt, createdAt, LocalDateTime.now()
        );
    }

}
