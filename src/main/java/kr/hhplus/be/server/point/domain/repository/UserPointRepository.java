package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;

public interface UserPointRepository {

    UserPointJPA findById(Long id);
    UserPointJPA save(UserPointJPA userPointJPA);
    void deleteAll();  // 테스트용
}
