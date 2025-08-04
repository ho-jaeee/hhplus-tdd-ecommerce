package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.service.dto.Order;

public interface OrderSaveService {

    Order save(Order order);
}
