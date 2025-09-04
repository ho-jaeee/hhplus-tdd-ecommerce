package kr.hhplus.be.server.couponRedisKafka.consumer;

import kr.hhplus.be.server.couponRedisKafka.domain.Service.CouponIssueServiceNew; // 코어 서비스
import kr.hhplus.be.server.couponRedisKafka.event.IssueRequestEvent;
import kr.hhplus.be.server.couponRedisKafka.event.IssueResultEvent;
import kr.hhplus.be.server.couponRedisKafka.port.GatekeeperPort;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.IssueResultCode;
import kr.hhplus.be.server.couponRedisKafka.port.EventBusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;


@Component("couponIssueConsumer")
@RequiredArgsConstructor
public class CouponIssueConsumer {

    private final CouponIssueServiceNew core;
    private final EventBusPort eventBus;
    private final GatekeeperPort gatekeeper;

    @KafkaListener(
            id = "coupon-consumer",
            topics = "coupon.issue.requests.v2",
            groupId = "coupon-issue-v2",
            containerFactory = "issueReqListenerContainerFactory"
    )
    public void onIssue(IssueRequestEvent e, Acknowledgment ack) {
        try {
            // 1) 사전 게이트: 순서 아니면 보류등록 후 스킵
            if (!gatekeeper.gate(e.couponId(), e.rank())) {
                gatekeeper.enqueuePending(e.couponId(), e.requestId(), e.rank());
                ack.acknowledge();
                return;
            }

            // 2) 코어 트랜잭션 확정
            var r = core.CouponIssueIdempotent(e.couponId(), e.userId(), e.requestId());

            // 3) 사후 해제: 성공 시 next++ 및 연속 구간 해제
            if (r.committed()) {
                gatekeeper.advance(e.couponId(), 1);
                gatekeeper.releaseContinuous(e.couponId());
            } else if (r.reason() == CouponIssueServiceNew.Reason.IDEMPOTENT_HIT) {
                gatekeeper.releaseContinuous(e.couponId()); // 선택
            }

            // 4) 결과 이벤트 발행
            var code = switch (r.reason()) {
                case SUCCESS -> IssueResultCode.SUCCESS;
                case OUT_OF_STOCK -> IssueResultCode.OUT_OF_STOCK;
                case IDEMPOTENT_HIT -> IssueResultCode.IDEMPOTENT_HIT;
                default -> IssueResultCode.ERROR;
            };
            eventBus.publishIssueResult(new IssueResultEvent(
                    e.requestId(), e.couponId(), e.userId(),
                    r.committed(), code, r.couponUserId(), Instant.now()
            ));

            ack.acknowledge();
        } catch (Exception ex) {
            // 여기서 예외를 던져 컨테이너 에러핸들러(DefaultErrorHandler)로 위임
            throw ex;
        }
    }
}