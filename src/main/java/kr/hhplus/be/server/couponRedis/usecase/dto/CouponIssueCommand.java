package kr.hhplus.be.server.couponRedis.usecase.dto;

public record CouponIssueCommand(Long couponId, Long userId, String reqId) {}