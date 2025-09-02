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
        return jpaRepository.findById(id)
                .orElseGet(() -> {
                    UserPointJPA newUser = UserPointJPA.empty(id); // 팩토리 메서드
                    return jpaRepository.save(newUser);
                });

    }

    @Override
    public UserPointJPA save(UserPointJPA userPointJPA) {
        return jpaRepository.save(userPointJPA);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

}
