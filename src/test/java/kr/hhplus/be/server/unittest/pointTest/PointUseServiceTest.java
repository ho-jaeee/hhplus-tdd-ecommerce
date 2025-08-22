package kr.hhplus.be.server.unittest.pointTest;


import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class PointUseServiceTest {

    @Mock
    UserPointRepository userPointRepository;

    @Mock
    UserPointHistoryRepository userPointHistoryRepository;

    @InjectMocks
    PointUseService pointUseService;

    @Test
    void UsePointTest() {

        //given
        long userId = 123L;
        long usePoint = 8000L;
        long existingPoint = 10000L;
        UserPointJPA existing = new UserPointJPA(userId, existingPoint,System.currentTimeMillis());
        // 고정된 ID 1L에 대해 항상 이 UserPoint를 줘야 할 때
        Mockito.when(userPointRepository.findById(userId)).thenReturn(existing);

        // 저장할 때마다 들어오는 객체가 다를 수 있음 → 그대로 리턴
        Mockito.when(userPointRepository.save(Mockito.any(UserPointJPA.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        /*when*/
        UserPointJPA result = pointUseService.usePoint(userId, usePoint);

        //then(결과 검증)
        Assertions.assertThat(result.getPoint()).as("기존 포인트에 충전 금액이 더해져야 함")
                .isEqualTo(existingPoint - usePoint);//Stub

        // 히스토리 저장 검증
        Mockito.verify(userPointHistoryRepository, Mockito.times(1))
                .save(Mockito.argThat(history ->
                        history.getUserId().equals(userId)
                                && history.getPoint().equals(usePoint)
                                && history.getType() == UserPointHistoryJPA.TransactionType.USE
                ));

    }
    @Test
    @DisplayName("포인트가 부족하면 예외가 발생해야 한다 (잔액 3,000, 사용 요청 5,000)")
    void shouldThrowExceptionWhenPointIsNotEnough() {
        // Given
        UserPointJPA userPoint = new UserPointJPA(1L, 3000L, System.currentTimeMillis());

        // When & Then
        assertThatThrownBy(() -> userPoint.use(5000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트가 부족");
    }
}



