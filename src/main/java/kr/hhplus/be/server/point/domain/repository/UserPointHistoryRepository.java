package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;

import java.util.List;


public interface UserPointHistoryRepository {

    UserPointHistoryJPA save(UserPointHistoryJPA userPointHistoryJPA);
     List<UserPointHistoryJPA> findAll();
}
