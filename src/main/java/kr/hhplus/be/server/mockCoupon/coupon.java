package kr.hhplus.be.server;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/coupon")
@Tag(name = "Coupon", description = "쿠폰 관련 API")
public class coupon {

    @Operation(summary = "선착순 쿠폰 발급", description = "사용자에게 선착순으로 쿠폰을 발급합니다.")
    @PostMapping("/issue")
    public ResponseEntity<couponRespone> issueCoupon(
            @RequestBody couponRequest request
    ) {
        // 🔥 실제 발급 로직 없이 Mock 응답, 시뮬레이션을 위해서 ID수량으로 겟수를 판단하게 만들었음
        if (request.userId() == 999L) {
            // 예: 999번 유저는 쿠폰 다 소진된 상황
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new couponRespone(null, request.userId(), true, "쿠폰이 모두 소진되었습니다."));
        }

        return ResponseEntity.ok(new couponRespone(
                1L, // mock couponId
                request.userId(),
                false,
                "2025-07-17T21:00:00Z"
        ));
    }

    @Operation(summary = "사용자 보유 쿠폰 목록 조회", description = "user_coupon 기준으로 사용자가 보유한 쿠폰 리스트를 반환합니다.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<userCouponRespone>> getUserCoupons(@PathVariable Long userId) {

        // 🔥 Mock 데이터
        List<userCouponRespone> coupons = new ArrayList<>();

        if (userId == 123L) {
            coupons.add(new userCouponRespone(123L, "10% 할인 쿠폰", false, "2025-07-17T20:00:00", null));
            coupons.add(new userCouponRespone(234L, "무료배송 쿠폰", true, "2025-07-10T19:00:00", "2025-07-15T15:30:00"));
        }

        return ResponseEntity.ok(coupons);
    }
}
