package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.domain.model.OrderHistoryJPA;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOrderHistoryRepository extends JpaRepository<OrderHistoryJPA, Long> {
}
