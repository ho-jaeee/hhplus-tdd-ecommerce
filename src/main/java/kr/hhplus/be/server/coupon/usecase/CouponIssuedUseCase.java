package kr.hhplus.be.server.coupon.usecase;


import kr.hhplus.be.server.coupon.usecase.dto.CouponIssueCommand;
import kr.hhplus.be.server.coupon.usecase.dto.CouponIssueResult;

public interface CouponIssuedUseCase {

    CouponIssueResult issueCoupon(CouponIssueCommand command);

}
