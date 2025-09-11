package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.model.OrderKafkaItemJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderKafkaItemRepository extends JpaRepository<OrderKafkaItemJPA, Long> {

    List<OrderKafkaItemJPA> findByOrderEvent_OrderId(Long orderId);

    boolean existsByOrderEvent_OrderIdAndProductId(Long orderId, Long productId);

    long countByOrderEvent_OrderId(Long orderId);

    void deleteByOrderEvent_OrderId(Long orderId);
}
