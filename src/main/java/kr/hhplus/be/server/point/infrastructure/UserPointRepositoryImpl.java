package kr.hhplus.be.server.point.infrastructure;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserPointRepositoryImpl implements UserPointRepository {

    private final SpringDataUserPointRepository jpaRepository;

    public UserPointRepositoryImpl(SpringDataUserPointRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }


    @Override
    public UserPointJPA findById(Long id) {
        return (jpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.: " + id)));

    }

    @Override
    public UserPointJPA save(UserPointJPA userPointJPA) {
        return jpaRepository.save(userPointJPA);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public Optional<UserPointJPA> findByIdForUpdate(Long userId) {
        return jpaRepository.findByIdForUpdate(userId);


    }
}
