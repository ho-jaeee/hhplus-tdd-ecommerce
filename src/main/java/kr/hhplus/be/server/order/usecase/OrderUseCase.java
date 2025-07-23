package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.order.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.model.OrderJPA;

public interface OrderUseCase {

    OrderJPA createOrder(OrderRequest request);

}
