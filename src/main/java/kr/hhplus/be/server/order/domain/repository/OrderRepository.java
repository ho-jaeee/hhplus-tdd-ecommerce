package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.model.OrderJPA;

public interface OrderRepository {

    OrderJPA insert(OrderJPA order);

    void update(OrderJPA order);

    OrderJPA findByOrderId(long orderId);

}
