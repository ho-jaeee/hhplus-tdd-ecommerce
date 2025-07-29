package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.model.Order;
import kr.hhplus.be.server.order.domain.model.OrderJPA;

public interface OrderHistoryService {

    void orderInsert(Order order, String reason);
}
