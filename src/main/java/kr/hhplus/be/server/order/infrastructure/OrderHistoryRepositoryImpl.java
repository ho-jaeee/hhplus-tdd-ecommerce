package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.database.order.OrderHistoryTable;
import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;
import kr.hhplus.be.server.order.domain.repository.OrderHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderHistoryRepositoryImpl implements OrderHistoryRepository {

    private OrderHistoryTable orderHistoryTable;


    @Override
    public OrderHistoryJPA insert(OrderHistoryJPA history) {
        return orderHistoryTable.insert(history);
    }

    @Override
    public List<OrderHistoryJPA> findByOrderId(long orderId) {
        return orderHistoryTable.findByOrderId(orderId);
    }
}
