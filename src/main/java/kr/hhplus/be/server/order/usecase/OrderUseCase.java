package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.order.domain.model.OrderCommand;
import kr.hhplus.be.server.order.domain.model.OrderResult;

public interface OrderUseCase {

    OrderResult createOrder(OrderCommand command);

}
