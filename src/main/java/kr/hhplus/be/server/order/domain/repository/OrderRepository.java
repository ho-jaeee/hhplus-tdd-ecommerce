package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.model.OrderJPA;

public interface OrderRepository {

    OrderJPA save(OrderJPA order);

    void update(OrderJPA order);

    OrderJPA findByOrderId(long orderId);

}
