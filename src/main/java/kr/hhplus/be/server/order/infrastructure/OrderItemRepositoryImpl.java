package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import kr.hhplus.be.server.order.domain.service.dto.OrderItem;
import org.springframework.stereotype.Repository;


@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {


    private final SpringDataOrderItemRepository jpaRepository;

    public OrderItemRepositoryImpl(SpringDataOrderItemRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderItemJPA save(OrderItemJPA orderItemJPA) {
        return jpaRepository.save(orderItemJPA);
    }
}
