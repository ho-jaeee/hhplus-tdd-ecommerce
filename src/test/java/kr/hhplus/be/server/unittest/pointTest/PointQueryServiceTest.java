package kr.hhplus.be.server.unittest.pointTest;



import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.domain.service.PointQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class PointQueryServiceTest {

    @Mock
    private UserPointRepository userPointRepository;


    @InjectMocks
    private PointQueryService PointQueryService;


    @Test
    void GetPointTest() {

        //given
        long userId = 1L;
        long existingPoint = 10000L;
        UserPointJPA existing = new UserPointJPA (userId, existingPoint,System.currentTimeMillis());
        Mockito.when(userPointRepository.findById(userId)).thenReturn(Optional.of(existing));
        /*when*/
        UserPointJPA  result = PointQueryService.GetPoint(userId);

        //then(결과 검증)
        assertThat(result.getPoint()).isEqualTo(existingPoint);

        // mock이 실제 호출되었는지 검증
        Mockito.verify(userPointRepository, Mockito.times(1)).findById(userId);

    }

}
