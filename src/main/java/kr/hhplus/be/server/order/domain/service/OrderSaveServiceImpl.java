package kr.hhplus.be.server.order.domain.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSaveServiceImpl implements OrderSaveService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJPA saved = orderRepository.save(order.toEntity());
        return order.withId(saved.getOrderId());
    }
}
