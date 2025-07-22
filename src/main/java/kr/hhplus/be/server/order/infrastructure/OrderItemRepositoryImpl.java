package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.database.order.OrderItemTable;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private OrderItemTable orderItemTable;

    @Override
    public OrderItemJPA insert(OrderItemJPA item) {
        return orderItemTable.insert(item);
    }

    @Override
    public List<OrderItemJPA> findByOrderId(long orderId) {
        return orderItemTable.findByOrderId(orderId);
    }

    @Override
    public OrderItemJPA selectById(long Id) {
        return orderItemTable.selectById(Id);
    }
}
