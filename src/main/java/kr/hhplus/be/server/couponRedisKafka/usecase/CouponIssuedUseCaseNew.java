package kr.hhplus.be.server.couponRedisKafka.usecase;

import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueCommandNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueResultNew;

public interface CouponIssuedUseCaseNew {
    CouponIssueResultNew issue(CouponIssueCommandNew command);
}
