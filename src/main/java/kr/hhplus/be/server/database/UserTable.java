package kr.hhplus.be.server.database;


import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class UserTable {
    private final Map<Long, UserPointJPA> table = new HashMap<>();

    public UserPointJPA selectById(Long id) {
        throttle(200);
        return table.getOrDefault(id, UserPointJPA.empty(id));
    }

    public UserPointJPA insertOrUpdate(long id, long amount) {
        throttle(300);
        UserPointJPA userPoint = new UserPointJPA(id, amount, System.currentTimeMillis());
        table.put(id, userPoint);
        return userPoint;
    }

    private void throttle(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
        } catch (InterruptedException ignored) {

        }
    }

}
