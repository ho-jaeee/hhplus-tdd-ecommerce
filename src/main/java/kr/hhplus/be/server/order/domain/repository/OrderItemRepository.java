package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.model.OrderItemJPA;

import java.util.List;

public interface OrderItemRepository {

    OrderItemJPA insert(OrderItemJPA item);

    List<OrderItemJPA> findByOrderId(long orderId);

    OrderItemJPA selectById(long Id);
}
