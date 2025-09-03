package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.model.OrderKafkaJPA;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderKafkaRepository extends JpaRepository<OrderKafkaJPA, Long> {
    // N+1 방지: 헤더 + 아이템 즉시 로딩(조회용)
    @EntityGraph(attributePaths = "items")
    Optional<OrderKafkaJPA> findWithItemsByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);
}
