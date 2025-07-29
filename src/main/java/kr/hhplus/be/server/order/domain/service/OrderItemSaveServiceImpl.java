package kr.hhplus.be.server.order.domain.service;

import kr.hhplus.be.server.order.domain.model.OrderItem;
import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import kr.hhplus.be.server.order.domain.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderItemSaveServiceImpl implements OrderItemSaveService {

    private final OrderItemRepository orderItemRepository;

    @Override
    public List<OrderItemJPA> itemSave(Long orderId, List<OrderItem> items) {
        return items.stream()
                .map(item -> orderItemRepository.insert(item.toEntity(orderId)))
                .toList();
    }
}
