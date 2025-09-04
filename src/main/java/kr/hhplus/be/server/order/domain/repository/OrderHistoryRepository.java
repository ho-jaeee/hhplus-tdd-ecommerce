package kr.hhplus.be.server.order.domain.repository;


import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;

public interface OrderHistoryRepository{

    OrderHistoryJPA save(OrderHistoryJPA orderHistoryJPA);

}
