package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.model.Order;
import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;
import kr.hhplus.be.server.order.domain.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    @Override
    public void orderInsert(Order order, String reason) {
      orderHistoryRepository.insert(order.toHistory(reason));
    }
}
