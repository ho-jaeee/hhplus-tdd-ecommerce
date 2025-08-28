package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;
import kr.hhplus.be.server.order.domain.repository.OrderHistoryRepository;
import org.springframework.stereotype.Repository;


@Repository
public class OrderHistoryRepositoryImpl implements OrderHistoryRepository {

    private final SpringDataOrderHistoryRepository jpaRepository;

    public OrderHistoryRepositoryImpl(SpringDataOrderHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrderHistoryJPA save(OrderHistoryJPA orderHistoryJPA) {
        return jpaRepository.save(orderHistoryJPA);
    }
}
