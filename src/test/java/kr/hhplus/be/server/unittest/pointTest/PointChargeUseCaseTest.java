package kr.hhplus.be.server.unittest.pointTest;


import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.service.PointChargeService;
import kr.hhplus.be.server.point.usecase.PointChargeUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;



@ExtendWith(MockitoExtension.class)
public class PointChargeUseCaseTest {

    @Mock
    PointChargeService pointChargeService;

    @InjectMocks
    PointChargeUseCase pointChargeUseCase;


    @Test
    void ChargePointSuccess() {

        //given


        long userId = 1L;
        long point = 5000L;

        UserPointJPA update = new  UserPointJPA(userId, 1000L, System.currentTimeMillis());
        update.charge(point);

        given(pointChargeService.ChargePoint(userId, point)).willReturn(update);

        // when
        PointResponse result = pointChargeUseCase.ChargeUseCase(userId, point);

        // then
        assertThat(result.point()).isEqualTo(6000L);
        verify(pointChargeService).ChargePoint(userId, point);

    }

    @Test
    void ChargePointNotZeroTest() {
        // given

        long userId = 1234L;
        long point = 0L;

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.ChargeUseCase(userId, point))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원 초과여야 합니다.");

        verify(pointChargeService, never()).ChargePoint(anyLong(), anyLong());
    }

    @Test
    void ChargePointUnitTest() {
        // given
        long userId = 1L;
        long point = 1234L;

        // when & then
        assertThatThrownBy(() -> pointChargeUseCase.ChargeUseCase(userId, point))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 1,000원 단위로만 가능합니다.");

        verify(pointChargeService, never()).ChargePoint(anyLong(), anyLong());
    }
}
