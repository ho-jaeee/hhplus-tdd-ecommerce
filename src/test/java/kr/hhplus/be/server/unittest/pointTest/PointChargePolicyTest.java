package kr.hhplus.be.server.unittest.pointTest;

import kr.hhplus.be.server.point.policy.PointChargePolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class PointChargePolicyTest {

    @Test
    void ChargeNotZero(){
        long point = 0L;

        assertThatThrownBy(() -> PointChargePolicy.PointChargeValidate(point))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 0원 초과여야 합니다.");
    }
    @Test
    void ChargePointUnit() {
        long pint = 1250L;

        assertThatThrownBy(() -> PointChargePolicy.PointChargeValidate(pint))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("충전 금액은 1,000원 단위로만 가능합니다.");
    }





}
