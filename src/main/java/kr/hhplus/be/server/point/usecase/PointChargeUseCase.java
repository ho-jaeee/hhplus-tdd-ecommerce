package kr.hhplus.be.server.point.usecase;

import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.policy.PointChargePolicy;
import kr.hhplus.be.server.point.domain.service.PointChargeService;
import org.springframework.stereotype.Service;

@Service
public class PointChargeUseCase {

    private final PointChargeService pointChargeService;

    public PointChargeUseCase(PointChargeService pointChargeService) {
        this.pointChargeService = pointChargeService;
    }

    //UseCase는 최대한 단순하게
    public PointResponse ChargeUseCase(long userId, long point) {
        PointChargePolicy.PointChargeValidate(point);
        UserPointJPA result = pointChargeService.ChargePoint(userId, point);
        return PointResponse.from(result);
    }

}

