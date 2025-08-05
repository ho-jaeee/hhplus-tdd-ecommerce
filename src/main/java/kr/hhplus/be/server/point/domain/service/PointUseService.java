package kr.hhplus.be.server.point.domain.service;


import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PointUseService {

    private final UserPointRepository userPointRepo;
    private final UserPointHistoryRepository userPointHistoryRepo;

    public PointUseService(UserPointRepository userPointRepo, UserPointHistoryRepository userPointHistoryRepo)
    {
        this.userPointRepo = userPointRepo;
        this.userPointHistoryRepo = userPointHistoryRepo;
    }


    public UserPointJPA usePoint(long userId, long point) {

        /*현재 포인트 조회
         **현재 포인트보다 포인트가 없으면 예외발생
         **포인트 사용
         */

        UserPointJPA current = userPointRepo.findById(userId);
        current.use(point);

        // 포인트 업데이트
        UserPointJPA updated = userPointRepo.save(current);

        /*히스토리 저장*/
        userPointHistoryRepo.save(
                new UserPointHistoryJPA(
                        null,
                        userId,
                        point,
                        UserPointHistoryJPA.TransactionType.USE,
                        System.currentTimeMillis()
                )
        );
        return updated;
    }

}
