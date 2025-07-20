package kr.hhplus.be.server.pointTest;


import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.domain.service.PointChargeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class PointChargeServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private UserPointHistoryRepository userPointHistoryRepository;

    @InjectMocks
    private PointChargeService pointChargeService;




    @Test
    void ChargePointTest() {

        //given
        long userId = 123L;
        long chargePoint = 10000L;
        long existingPoint = 10000L;
        UserPointJPA existing = new UserPointJPA(userId, existingPoint,System.currentTimeMillis());
        // 고정된 ID 1L에 대해 항상 이 UserPoint를 줘야 할 때
        Mockito.when(userPointRepository.selectById(userId)).thenReturn(existing);
        // 저장할 때마다 들어오는 객체가 다를 수 있음 → 그대로 리턴

        Mockito.when(userPointRepository.insertOrUpdate(Mockito.anyLong(), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    long id = invocation.getArgument(0);
                    long amount = invocation.getArgument(1);
                    return new UserPointJPA(id, amount, System.currentTimeMillis());
                });

        /*when*/
        long result = pointChargeService.ChargePoint(userId, chargePoint);

        //then(결과 검증)
        // then
        Assertions.assertThat(result).as("기존 포인트에 충전 금액이 더해져야 함")
                .isEqualTo(existingPoint + chargePoint);//Stub

        // findById()가 정확히 1번 호출됐는지 검증
        Mockito.verify(userPointRepository, Mockito.times(1)).selectById(userId);

        // save()도 정확히 1번 호출됐는지 검증
        Mockito.verify(userPointRepository, Mockito.times(1)).insertOrUpdate(Mockito.anyLong(), Mockito.anyLong());

    }

}
