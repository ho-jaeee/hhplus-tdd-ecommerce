package kr.hhplus.be.server.couponRedisKafka.usecase.dto;

import java.time.Instant;

public record CouponIssueCommandNew(Long couponId,
                                    Long userId,
                                    String requestId,
                                    Instant requestedAt
){}