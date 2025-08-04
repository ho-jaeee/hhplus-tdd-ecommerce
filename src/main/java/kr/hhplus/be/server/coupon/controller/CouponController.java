package kr.hhplus.be.server.coupon.controller;


import kr.hhplus.be.server.coupon.controller.dto.CouponRequest;
import kr.hhplus.be.server.coupon.controller.dto.CouponResponse;
import kr.hhplus.be.server.coupon.usecase.CouponIssueCommand;
import kr.hhplus.be.server.coupon.usecase.CouponIssueResult;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {

    private final CouponIssuedUseCase couponIssuedUseCase;

    @PostMapping("/issue")
    public ResponseEntity<CouponResponse> issueCoupon(@RequestBody CouponRequest request) {
        CouponIssueCommand command = new CouponIssueCommand(request.userId(), request.couponId());
        CouponIssueResult result = couponIssuedUseCase.issueCoupon(command);

        CouponResponse response = new CouponResponse(result.userId(), result.couponId(), result.message());

        if ("쿠폰 발급 성공".equals(result.message())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

}
