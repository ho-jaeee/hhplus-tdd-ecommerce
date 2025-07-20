package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.UserPointHistory;

import java.util.List;

public interface UserPointHistoryRepository {

    /***
     유저 ID로 포인트 사용 이력 정보를 조회합니다.
     ***/
    List<UserPointHistory> selectByUserId(long userId);

    /***
     포인트 사용 이력을 저장합니다.(Charge, Use)
     ***/
    void insert(UserPointHistory history);
}
