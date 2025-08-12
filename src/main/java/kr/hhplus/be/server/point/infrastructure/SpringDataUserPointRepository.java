package kr.hhplus.be.server.point.infrastructure;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataUserPointRepository extends JpaRepository<UserPointJPA, Long> {
}
