package kr.hhplus.be.server.common;

public interface DomainEventPublisher {

    void publish(Object event);
}
