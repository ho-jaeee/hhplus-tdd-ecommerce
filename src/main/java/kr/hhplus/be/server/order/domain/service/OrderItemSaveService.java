package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.service.dto.OrderItem;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;

import java.util.List;

public interface OrderItemSaveService {

    List<OrderItemJPA> itemSave(Long orderId, List<OrderItem> items);
}
