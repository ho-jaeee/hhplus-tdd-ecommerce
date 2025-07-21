package kr.hhplus.be.server.database;

import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class UserHistoryTable {


        private final List<UserPointHistoryJPA> table = new ArrayList<>();
        private long cursor = 1;

        public void insert(long userId, long amount, UserPointHistoryJPA.TransactionType type, long updateMillis) {
            throttle(300L);
            UserPointHistoryJPA pointHistory = new UserPointHistoryJPA(cursor++, userId, amount, type, updateMillis);
            table.add(pointHistory);
        }

        public List<UserPointHistoryJPA> selectAllByUserId(long userId) {
            return table.stream().filter(pointHistory -> pointHistory.getUserId() == userId).toList();
        }

        private void throttle(long millis) {
            try {
                TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
            } catch (InterruptedException ignored) {

            }
        }
    }

