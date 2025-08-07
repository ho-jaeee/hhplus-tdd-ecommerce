package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.model.OrderItemJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderItemRepository extends JpaRepository<OrderItemJPA, Long> {
}
