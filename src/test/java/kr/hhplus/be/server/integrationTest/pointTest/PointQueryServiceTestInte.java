package kr.hhplus.be.server.integrationTest.pointTest;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.domain.service.PointQueryService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PointQueryServiceTestInte {

    @Autowired
    private UserPointRepository userPointRepository;


    @Autowired
    private PointQueryService pointQueryService;

    @Test
    @DisplayName("사용자 포인트 조회 테스트 - 통합 테스트")
    void getPointTest() {
        // given
        long userId = 1L;
        long existingPoint = 10000L;
        UserPointJPA existing = new UserPointJPA(userId, existingPoint, System.currentTimeMillis());
        userPointRepository.save(existing);

        // when
        UserPointJPA result = pointQueryService.GetPoint(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPoint()).isEqualTo(existingPoint);
    }
}
