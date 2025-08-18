package kr.hhplus.be.server.common.event;

public interface DomainEventPublisher {

    void publish(Object event);
}
