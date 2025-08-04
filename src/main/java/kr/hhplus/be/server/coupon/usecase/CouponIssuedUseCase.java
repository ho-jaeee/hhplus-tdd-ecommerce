package kr.hhplus.be.server.coupon.usecase;


public interface CouponIssuedUseCase {

    CouponIssueResult issueCoupon(CouponIssueCommand command);

}
