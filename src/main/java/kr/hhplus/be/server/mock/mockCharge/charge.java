package kr.hhplus.be.server.mock.mockCharge;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/point")
@Tag(name = "Charge", description = "포인트 조회/충전 관련 API")
public class charge {

    @Operation(summary = "포인트 충전(Mock)", description = "사용자가 포인트를 충전하는 API")
    @PostMapping("/charge/{id}")
    public ResponseEntity<chargeRespone> chargePoint(
            @PathVariable Long id,
            @RequestBody chargeRequest request
    ) {
        //Entity 없이 하드코딩 응답
        long chargedAmount = request.amount();
        long totalPoint =+ chargedAmount;

        chargeRespone response = new chargeRespone(id, chargedAmount, totalPoint);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 조회(Mock)", description = "사용자가 포인트 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<chargeRespone> getPointBalance(@PathVariable Long id) {
        //Mock 응답 - 실제 DB 없이 임시 포인트 반환
        chargeRespone response = new chargeRespone(id, 105000L, System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}