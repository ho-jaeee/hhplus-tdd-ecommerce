package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;

public interface OrderUseCase {

    OrderResult createOrder(OrderCommand command);

}
