package kr.hhplus.be.server.coupon.policy;

import kr.hhplus.be.server.coupon.domain.service.Coupon;
import kr.hhplus.be.server.coupon.domain.service.CouponUser;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CouponValidator {
    public void validateIssue(Coupon coupon, Optional<CouponUser> couponUser) {
        if (!coupon.isValidPeriod()) {
            throw new IllegalStateException("쿠폰이 유효기간이 아닙니다.");
        }
        if (!coupon.canIssueMore()) {
            throw new IllegalStateException("발급 수량을 초과했습니다.");
        }
        if (couponUser.isPresent()) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }
    }

}
