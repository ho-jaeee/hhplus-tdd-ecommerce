package kr.hhplus.be.server.point.controller;


import kr.hhplus.be.server.point.controller.dto.PointChargeRequest;
import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.usecase.PointChargeUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/point")
public class PointChargeController {

    private final PointChargeUseCase pointChargeUseCase;

    public PointChargeController(PointChargeUseCase pointChargeUseCase) {
        this.pointChargeUseCase = pointChargeUseCase;
    }

    @PatchMapping("/charge/{userId}")
    public ResponseEntity<PointResponse> chargePoint(
            @PathVariable long userId,
            @RequestBody PointChargeRequest request
    ) {
        PointResponse result = pointChargeUseCase.ChargeUseCase(userId, request.point());
        return ResponseEntity.ok(PointResponse.of(result.userId(), result.point()));
    }

}
