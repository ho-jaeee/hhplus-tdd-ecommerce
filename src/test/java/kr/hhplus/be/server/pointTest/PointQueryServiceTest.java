package kr.hhplus.be.server.pointTest;


import kr.hhplus.be.server.point.domain.model.UserPoint;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.domain.service.PointQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class PointQueryServiceTest {

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private UserPointHistoryRepository userPointHistoryRepository;

    @InjectMocks
    private PointQueryService PointQueryService;


    @Test
    void GetPointTest() {

        //given
        long userId = 1L;
        long existingPoint = 10000L;
        UserPoint existing = new UserPoint(userId, existingPoint,System.currentTimeMillis());
        Mockito.when(userPointRepository.selectById(userId)).thenReturn(existing);

        /*when*/
        UserPoint result = PointQueryService.GetPoint(userId);

        //then(결과 검증)
        assertThat(result.point()).isEqualTo(existingPoint);

        // mock이 실제 호출되었는지 검증
        Mockito.verify(userPointRepository, Mockito.times(1)).selectById(userId);

    }

}
