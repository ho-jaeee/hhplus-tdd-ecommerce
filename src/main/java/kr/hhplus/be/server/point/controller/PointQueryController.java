package kr.hhplus.be.server.point.controller;


import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.usecase.PointQueryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/point")
public class PointQueryController {

    private final PointQueryUseCase pointQueryUseCase;

    public PointQueryController(PointQueryUseCase pointQueryUseCase) {
        this.pointQueryUseCase = pointQueryUseCase;
    }

    // 포인트 조회 API
    @GetMapping("/{id}")
    public ResponseEntity<PointResponse> getUserPoint(@PathVariable("id") long userId) {
        PointResponse result = pointQueryUseCase.QueryUseCase(userId);
        return ResponseEntity.ok(result);
    }
}
