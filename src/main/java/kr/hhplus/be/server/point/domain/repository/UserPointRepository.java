package kr.hhplus.be.server.point.domain.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPointRepository {

    UserPointJPA findById(Long id);
    UserPointJPA save(UserPointJPA userPointJPA);
    void deleteAll();// 테스트용

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserPointJPA u WHERE u.userId = :userId")
    @QueryHints(@QueryHint(name="jakarta.persistence.lock.timeout", value="2000"))
    Optional<UserPointJPA> findByIdForUpdate(@Param("userId") Long userId);
}
