//package kr.hhplus.be.server.point.component;
//
//
//import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
//import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.boot.CommandLineRunner;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//@Component
//@RequiredArgsConstructor
//public class UserPointHistorySeeder implements CommandLineRunner {
//
//    private final UserPointHistoryRepository repository;
//
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("Seeder 시작됨");
//        List<UserPointHistoryJPA> bulk = new ArrayList<>();
//        Random random = new Random();
//
//        for (int i = 0; i < 100_000; i++) {
//            Long userId = (long) (random.nextInt(1000) + 1); // 1~1000 사이 유저 ID
//            Long point = (long) (random.nextInt(10000) + 100); // 100~10100 포인트
//            UserPointHistoryJPA.TransactionType type =
//                    (i % 2 == 0) ? UserPointHistoryJPA.TransactionType.CHARGE : UserPointHistoryJPA.TransactionType.USE;
//
//            UserPointHistoryJPA history = UserPointHistoryJPA.create(userId, point, type);
//            bulk.add(history);
//
//            if (bulk.size() == 1000) { // 1000건 단위로 저장
//                repository.saveAll(bulk);
//                bulk.clear();
//            }
//
//        }
//
//        // 남은 레코드 처리
//        if (!bulk.isEmpty()) {
//            repository.saveAll(bulk);
//        }
//        System.out.println("Seeder 완료");
//        System.out.println("10만건의 UserPointHistoryJPA 더미 데이터 생성 완료");
//
//    }
//}
