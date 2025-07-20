package kr.hhplus.be.server.point.infrastructure;

import kr.hhplus.be.server.point.domain.model.UserPointHistory;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Repository
public class MomoryUserPointHistoryRepository implements UserPointHistoryRepository {

    private final List<UserPointHistory> store = new CopyOnWriteArrayList<>();
    private long sequence = 1L;

    @Override
    public void insert(UserPointHistory history) {
        UserPointHistory newHistory = new UserPointHistory(
                sequence++,
                history.userId(),
                history.point(),
                history.type(),
                System.currentTimeMillis()
        );
        store.add(newHistory);
    }

    @Override
    public List<UserPointHistory> selectByUserId(long userId) {
        return store.stream()
                .filter(history -> history.userId() == userId)
                .collect(Collectors.toList());
    }


}
