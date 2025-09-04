package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;

public interface OrderRedisUseCase {

    OrderResult createOrder(OrderCommand command);

}
