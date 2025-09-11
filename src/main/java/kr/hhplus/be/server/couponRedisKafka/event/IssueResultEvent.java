package kr.hhplus.be.server.couponRedisKafka.event;

import kr.hhplus.be.server.couponRedisKafka.usecase.dto.IssueResultCode;

import java.time.Instant;

public record IssueResultEvent(
        String requestId,
        Long couponId,
        Long userId,
        boolean success,
        IssueResultCode code,
        Long couponUserId,
        Instant occurredAt

) {}
