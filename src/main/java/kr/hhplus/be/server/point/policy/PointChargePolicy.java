package kr.hhplus.be.server.point.policy;


import org.springframework.stereotype.Component;

@Component
public class PointChargePolicy {

    public static void PointChargeValidate(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0원 초과여야 합니다.");
        }
        if (amount % 1000 != 0) {
            throw new IllegalArgumentException("충전 금액은 1,000원 단위로만 가능합니다.");
        }
    }
}
