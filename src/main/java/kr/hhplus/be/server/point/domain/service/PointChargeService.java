package kr.hhplus.be.server.point.domain.service;


import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PointChargeService {

    private final UserPointRepository userPointRepo;
    private final UserPointHistoryRepository userPointHistoryRepo;

    public PointChargeService(UserPointRepository userPointRepo, UserPointHistoryRepository userPointHistoryRepo) {
        this.userPointRepo = userPointRepo;
        this.userPointHistoryRepo = userPointHistoryRepo;
    }


    public UserPointJPA ChargePoint(long userId, long point) {

        /*현재 포인트 조회
        **계정이 없으면 새로운 계정을 만들고, 0포인트 반환함
        **포인트 충전
        */
        UserPointJPA current = userPointRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        current.charge(point);

        // 포인트 업데이트
        UserPointJPA updated = userPointRepo.save(current);

        /*히스토리 저장*/
        userPointHistoryRepo.save(
                new UserPointHistoryJPA(
                        null,
                        userId,
                        point,
                        UserPointHistoryJPA.TransactionType.CHARGE,
                        System.currentTimeMillis()
                )
        );
        return updated;
    }
}
