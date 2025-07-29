package kr.hhplus.be.server.order.usecase;

public interface OrderUseCase {

    OrderResult createOrder(OrderCommand command);

}
