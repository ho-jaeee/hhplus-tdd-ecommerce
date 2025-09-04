package kr.hhplus.be.server.couponRedisKafka.usecase;

import kr.hhplus.be.server.couponRedisKafka.event.IssueRequestEvent;
import kr.hhplus.be.server.couponRedisKafka.port.CouponRepositoryPort;
import kr.hhplus.be.server.couponRedisKafka.port.EventBusPort;
import kr.hhplus.be.server.couponRedisKafka.port.GatekeeperPort;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueCommandNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueResultNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.IssueResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.Instant;



@RequiredArgsConstructor
public class CouponIssuedUseCaseNewImpl implements CouponIssuedUseCaseNew {

    private final CouponRepositoryPort couponRepo;
    private final GatekeeperPort gatekeeper;
    private final EventBusPort eventBus;

    @Override
    public CouponIssueResultNew issue(CouponIssueCommandNew cmd) {
        // 1) 입력 검증 (null, 음수/0, 공백)
        if (cmd == null
                || cmd.couponId() == null || cmd.couponId() <= 0
                || cmd.userId() == null   || cmd.userId()   <= 0
                || isBlank(cmd.requestId())) {

            Long userId   = (cmd != null ? cmd.userId()   : null);
            Long couponId = (cmd != null ? cmd.couponId() : null);

            return CouponIssueResultNew.fail(userId, couponId,
                    IssueResultCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }

        // 2) 쿠폰 존재 확인
        if (!couponRepo.existsById(cmd.couponId())) {
            return CouponIssueResultNew.fail(cmd.userId(), cmd.couponId(), IssueResultCode.INVALID_COUPON, "INVALID_COUPON");
        }

        // 3) 선착순 rank 부여 (Redis 등)
        long rank = gatekeeper.assignRank(cmd.couponId(), cmd.userId(), cmd.requestId());

        // 4) REQ 이벤트 발행 (Kafka)
        Instant occurredAt = cmd.requestedAt() != null ? cmd.requestedAt() : Instant.now();
        IssueRequestEvent evt = new IssueRequestEvent(
                cmd.requestId(), cmd.couponId(), cmd.userId(), rank, occurredAt
        );
        eventBus.publishIssueRequest(evt);

        // 5) 요청 접수 응답
        return CouponIssueResultNew.ok(cmd.userId(), cmd.couponId(), rank, null /*couponUserId는 컨슈머 확정 후*/);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}

