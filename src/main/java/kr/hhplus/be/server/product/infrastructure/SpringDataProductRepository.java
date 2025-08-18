package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductRepository extends JpaRepository<ProductJPA, Long> {

    String findProductNameById(long id);
}
