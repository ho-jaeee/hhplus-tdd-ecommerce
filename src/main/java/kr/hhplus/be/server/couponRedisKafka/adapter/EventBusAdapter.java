package kr.hhplus.be.server.couponRedisKafka.adapter;

import kr.hhplus.be.server.couponRedisKafka.event.IssueRequestEvent;
import kr.hhplus.be.server.couponRedisKafka.event.IssueResultEvent;
import kr.hhplus.be.server.couponRedisKafka.port.EventBusPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventBusAdapter implements EventBusPort {
    private static final Logger log = LoggerFactory.getLogger(EventBusAdapter.class);

    private final KafkaTemplate<String, IssueRequestEvent> requestTemplate;
    private final KafkaTemplate<String, IssueResultEvent>  resultTemplate;

    private static final String TOPIC_REQ = "coupon.issue.requests.v2";
    private static final String TOPIC_RES = "coupon.issue.results.v2";


    @Override
    public void publishIssueRequest(IssueRequestEvent event) {
        // key = requestId 로 파티셔닝
        requestTemplate.send(TOPIC_REQ, event.requestId(), event);
    }

    @Override
    public void publishIssueResult(IssueResultEvent event) {
        resultTemplate.send(TOPIC_RES, event.requestId(), event);
    }
}
