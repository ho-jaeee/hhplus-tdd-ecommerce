package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPointRepository extends JpaRepository<UserPointJPA, Long> {
}
