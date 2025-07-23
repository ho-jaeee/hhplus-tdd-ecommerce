package kr.hhplus.be.server.order.infrastructure;


import kr.hhplus.be.server.database.order.OrderTable;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private OrderTable orderTable;

    @Override
    public OrderJPA save(OrderJPA order) {return orderTable.insert(order);}

    @Override
    public void update(OrderJPA order) {orderTable.update(order);}

    @Override
    public OrderJPA findByOrderId(long orderId) {return orderTable.findByOrderId(orderId);}
}
