package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserPointHistoryRepository extends JpaRepository<UserPointHistoryJPA, Long> {
}
