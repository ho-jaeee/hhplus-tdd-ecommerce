package kr.hhplus.be.server.point.domain.repository;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;

public interface UserPointRepository {

    /***
     유저 ID로 현재 보유 포인트 정보를 조회합니다.
     ***/
    UserPointJPA selectById(long userId);

    /***
     유저의 보유 포인트를 삽입하거나 업데이트합니다.
     ***/
    UserPointJPA insertOrUpdate(long userId, long point);
}
