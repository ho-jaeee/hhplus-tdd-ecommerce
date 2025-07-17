package kr.hhplus.be.server.mockOrder;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order", description = "주문 생성 및 결제 처리")
public class order {
    @Operation(summary = "주문 생성 (Mock)", description = "상품 주문을 생성하고 결제 처리 결과를 반환합니다.")
    @PostMapping
    public ResponseEntity<orderResponse> CreateOrder(@RequestBody orderRequest request) {

        // 조건에 따른 Mock 처리
        if (request.quantity() > 10) {
            return ResponseEntity.ok(new orderResponse(
                    null,
                    OrderStatus.FAILED,
                    null,
                    null,
                    "재고 부족"
            ));
        }

        if (request.userId() == 500L) {
            return ResponseEntity.ok(new orderResponse(
                    null,
                    OrderStatus.FAILED,
                    null,
                    null,
                    "포인트 부족"
            ));
        }

        if (request.couponId() != null && request.couponId() == 999L) {
            return ResponseEntity.ok(new orderResponse(
                    null,
                    OrderStatus.FAILED,
                    null,
                    null,
                    "유효하지 않은 쿠폰"
            ));
        }

        // 성공 응답
        return ResponseEntity.ok(new orderResponse(
                10001L,
                OrderStatus.PAID,
                100000L,
                80000L,
                "결제 완료"
        ));
    }
}