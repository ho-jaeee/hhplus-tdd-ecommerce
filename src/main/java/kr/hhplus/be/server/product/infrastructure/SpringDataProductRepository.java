package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.infrastructure.dto.ProductIdName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataProductRepository extends JpaRepository<ProductJPA, Long> {

    List<ProductIdName> findByProductIdIn(List<Long> ids);

}