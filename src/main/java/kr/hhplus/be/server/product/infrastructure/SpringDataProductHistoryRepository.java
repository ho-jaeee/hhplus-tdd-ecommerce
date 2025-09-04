package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductHistoryJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductHistoryRepository extends JpaRepository<ProductHistoryJPA, Long> {
}
