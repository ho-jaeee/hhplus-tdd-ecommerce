package kr.hhplus.be.server.point.infrastructure;

import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;

import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserPointHistoryRepositoryImpl implements UserPointHistoryRepository {

    private final SpringDataUserPointHistoryRepository jpaRepository;

    public UserPointHistoryRepositoryImpl(SpringDataUserPointHistoryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserPointHistoryJPA save(UserPointHistoryJPA userPointHistoryJPA) {
        return jpaRepository.save(userPointHistoryJPA);
    }

    @Override
    public List<UserPointHistoryJPA> findAll() {
        return jpaRepository.findAll();
    }
}
