package kr.hhplus.be.server.couponRedisKafka.port;


import kr.hhplus.be.server.couponRedisKafka.event.IssueRequestEvent;
import kr.hhplus.be.server.couponRedisKafka.event.IssueResultEvent;

// Kafka 등 이벤트 버스
public interface EventBusPort {
    void publishIssueRequest(IssueRequestEvent event);
    void publishIssueResult(IssueResultEvent event);
}
