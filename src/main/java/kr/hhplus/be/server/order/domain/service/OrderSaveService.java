package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.model.Order;

public interface OrderSaveService {

    Order save(Order order);
}
