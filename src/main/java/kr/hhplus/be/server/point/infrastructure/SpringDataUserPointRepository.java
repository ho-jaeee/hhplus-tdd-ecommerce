package kr.hhplus.be.server.point.infrastructure;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserPointRepository extends JpaRepository<UserPointJPA, Long> {
}
