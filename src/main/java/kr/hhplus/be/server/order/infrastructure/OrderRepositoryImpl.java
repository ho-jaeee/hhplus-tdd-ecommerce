package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final SpringDataOrderRepository jpaRepository;

    public OrderRepositoryImpl(SpringDataOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<OrderJPA> findById(Long orderId) {
        return jpaRepository.findById(orderId);
    }

    @Override
    public OrderJPA save(OrderJPA orderJPA) {
        return jpaRepository.save(orderJPA);
    }
}
