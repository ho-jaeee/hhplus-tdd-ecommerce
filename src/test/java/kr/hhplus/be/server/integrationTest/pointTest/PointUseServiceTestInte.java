package kr.hhplus.be.server.integrationTest.pointTest;


import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.point.domain.model.UserPointHistoryJPA;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointHistoryRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.domain.service.PointUseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class PointUseServiceTestInte {

    @Autowired
    private PointUseService pointUseService;

    @Autowired
    private UserPointRepository userPointRepository;

    @Autowired
    private UserPointHistoryRepository userPointHistoryRepository;

    @Test
    @DisplayName("포인트 사용 성공 테스트")
    void usePointTest() {
        // given
        long userId = 123L;
        long existingPoint = 10000L;
        long usePoint = 8000L;

        UserPointJPA userPoint = new UserPointJPA(userId, existingPoint, System.currentTimeMillis());
        userPointRepository.save(userPoint);

        // when
        UserPointJPA result = pointUseService.usePoint(userId, usePoint);

        // then
        assertThat(result.getPoint())
                .as("기존 포인트에서 사용 금액만큼 차감되어야 함")
                .isEqualTo(existingPoint - usePoint);

        // 히스토리 저장 확인
        assertThat(userPointHistoryRepository.findAll())
                .anySatisfy(history -> {
                    assertThat(history.getUserId()).isEqualTo(userId);
                    assertThat(history.getPoint()).isEqualTo(usePoint);
                    assertThat(history.getType()).isEqualTo(UserPointHistoryJPA.TransactionType.USE);
                });
    }

    @Test
    @DisplayName("포인트 부족 시 예외 발생 테스트")
    void shouldThrowExceptionWhenPointIsNotEnough() {
        // given
        long userId = 1L;
        UserPointJPA userPoint = new UserPointJPA(userId, 3000L, System.currentTimeMillis());
        userPointRepository.save(userPoint);

        // when & then
        assertThatThrownBy(() -> pointUseService.usePoint(userId, 5000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("포인트가 부족");
    }
}
