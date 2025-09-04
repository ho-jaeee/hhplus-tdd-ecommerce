package kr.hhplus.be.server.couponRedisKafka.event;

import java.time.Instant;

public record IssueRequestEvent(String requestId,
                                Long couponId,
                                Long userId,
                                Long rank,
                                Instant occurredAt
) {}
