package kr.hhplus.be.server.point.infrastructure;


import kr.hhplus.be.server.database.point.UserHistoryTable;
import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;

import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class MomoryUserPointHistoryRepository implements UserPointHistoryRepository {

    private UserHistoryTable userHistoryTable;

    @Override
    public List<UserPointHistoryJPA> selectByUserId(long userId) {
        return userHistoryTable.selectAllByUserId(userId);
    }

    @Override
    public void insert(UserPointHistoryJPA history) {
        userHistoryTable.insert(
                history.getUserId(),
                history.getPoint(),
                history.getType(),
                history.getUpdateMillis()
        );
    }




}
