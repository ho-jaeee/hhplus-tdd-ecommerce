package kr.hhplus.be.server.order.domain.repository;


import kr.hhplus.be.server.order.domain.model.OrderJPA;

import java.util.Optional;

public interface OrderRepository{

    Optional<OrderJPA> findById(Long orderId);
    OrderJPA save(OrderJPA orderJPA);
}
