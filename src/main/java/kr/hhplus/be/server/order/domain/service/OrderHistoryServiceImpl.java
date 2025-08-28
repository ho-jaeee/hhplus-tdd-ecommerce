package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.repository.OrderHistoryRepository;
import kr.hhplus.be.server.order.domain.service.dto.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    @Override
    public void orderInsert(Order order, String reason) {
      orderHistoryRepository.save(order.toHistory(reason));
    }
}
