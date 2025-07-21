package kr.hhplus.be.server.point.infrastructure;



import kr.hhplus.be.server.database.UserTable;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;


import org.springframework.stereotype.Repository;

@Repository
public class MemoryUserPointRepository implements UserPointRepository {



    private UserTable userTable;

    @Override
    public UserPointJPA selectById(long userId) {
        return userTable.selectById(userId);
    }

    @Override
    public UserPointJPA insertOrUpdate(long userId, long point) {
        return userTable.insertOrUpdate(userId, point);
    }
}
