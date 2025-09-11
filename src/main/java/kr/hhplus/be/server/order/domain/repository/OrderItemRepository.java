package kr.hhplus.be.server.order.domain.repository;


import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.service.dto.OrderItem;

public interface OrderItemRepository{

    OrderItemJPA save(OrderItemJPA orderItemJPA);

}
