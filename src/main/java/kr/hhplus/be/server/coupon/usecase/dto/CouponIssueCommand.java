package kr.hhplus.be.server.coupon.usecase.dto;

public record CouponIssueCommand(Long couponId, Long userId, String reqId) {}