package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;

public interface OrderTransactionService {


    OrderResult execute(OrderCommand command);
}
