package kr.hhplus.be.server.point.infrastructure;

import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUserPointHistoryRepository extends JpaRepository<UserPointHistoryJPA, Long> {
}
