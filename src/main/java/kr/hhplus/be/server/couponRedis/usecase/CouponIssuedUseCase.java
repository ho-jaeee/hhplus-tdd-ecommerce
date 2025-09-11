package kr.hhplus.be.server.couponRedis.usecase;


import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueCommand;
import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueResult;

public interface CouponIssuedUseCase {

    CouponIssueResult issueCoupon(CouponIssueCommand command);

}
