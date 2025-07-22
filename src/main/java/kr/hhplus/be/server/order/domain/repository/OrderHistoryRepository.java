package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;

import java.util.List;

public interface OrderHistoryRepository {

    OrderHistoryJPA insert(OrderHistoryJPA history);

    List<OrderHistoryJPA> findByOrderId(long orderId);
}
