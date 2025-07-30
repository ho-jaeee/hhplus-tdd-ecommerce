package kr.hhplus.be.server.integrationTest.pointTest;


import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.point.usecase.PointChargeUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class PointChargeUseCaseTestInte {

    @Autowired
    private PointChargeUseCase pointChargeUseCase;

    @Autowired
    private UserPointRepository userPointRepository;

    @Test
    void chargePointSuccess() {
        // given: 테스트용 사용자 데이터 준비
        long userId = 100L;
        long initialPoint = 1000L;
        long chargeAmount = 5000L;

        userPointRepository.save(new UserPointJPA(userId, initialPoint, System.currentTimeMillis()));

        // when: 충전 실행
        PointResponse response = pointChargeUseCase.ChargeUseCase(userId, chargeAmount);

        // then: DB에 반영된 결과 확인
        assertThat(response.point()).isEqualTo(initialPoint + chargeAmount);

        UserPointJPA updatedUserPoint = userPointRepository.findById(userId).orElseThrow();
        assertThat(updatedUserPoint.getPoint()).isEqualTo(initialPoint + chargeAmount);
    }

    @Test
    void chargePointNotZeroTest() {
        long userId = 101L;
        long chargeAmount = 0L;

        assertThatThrownBy(() -> pointChargeUseCase.ChargeUseCase(userId, chargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원 초과여야 합니다.");
    }

    @Test
    void chargePointUnitTest() {
        long userId = 102L;
        long invalidChargeAmount = 1234L;

        assertThatThrownBy(() -> pointChargeUseCase.ChargeUseCase(userId, invalidChargeAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 1,000원 단위로만 가능합니다.");
    }
}
