package kr.hhplus.be.server.point.domain.service;


import kr.hhplus.be.server.point.domain.model.UserPointHistory;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.springframework.stereotype.Service;



@Service
public class PointChargeService {

    private final UserPointRepository userPointRepo;
    private final UserPointHistoryRepository userPointHistoryRepo;

    public PointChargeService(UserPointRepository userPointRepo, UserPointHistoryRepository userPointHistoryRepo) {
        this.userPointRepo = userPointRepo;
        this.userPointHistoryRepo = userPointHistoryRepo;
    }

    public long ChargePoint(long userId, long point) {

        /*현재 포인트 조회
        **계정이 없으면 새로운 계정을 만들고, 0포인트 반환함
        */
        UserPointJPA current = userPointRepo.selectById(userId);

        /*포인트 충전*/
        long charge = current.charge(point);
        userPointRepo.insertOrUpdate(userId, charge);

        /*히스토리 저장*/
        userPointHistoryRepo.insert(
                new UserPointHistory(
                        0L,
                        userId,
                        point,
                        UserPointHistory.TransactionType.CHARGE,
                        System.currentTimeMillis()
                )
        );
        return charge;
    }
}
