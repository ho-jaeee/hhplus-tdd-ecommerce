package kr.hhplus.be.server.coupon.controller;


import kr.hhplus.be.server.coupon.controller.dto.CouponRequest;
import kr.hhplus.be.server.coupon.controller.dto.CouponResponse;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCase;
import kr.hhplus.be.server.coupon.usecase.dto.CouponIssueCommand;
import kr.hhplus.be.server.coupon.usecase.dto.CouponIssueResult;
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
        // 멱등키(reqId)는 유스케이스에서 비어있으면 UUID로 생성됨
        CouponIssueCommand command =
                new CouponIssueCommand(request.couponId(), request.userId(), request.reqId());

        CouponIssueResult result = couponIssuedUseCase.issueCoupon(command);

        // 응답 DTO 구성 (rank를 노출하고 싶지 않으면 제거)
        CouponResponse response = new CouponResponse(
                result.userId(),
                result.couponId(),
                result.success(),
                result.message(),
                result.rank()
        );

        // 메시지 기반 상태코드 매핑 (유스케이스/서비스 메시지와 합의 필요)
        HttpStatus status;
        if (result.success()) {
            status = HttpStatus.CREATED;                // 201: 발급 성공
        } else {
            String m = result.message();
            if ("잘못된 입력입니다.".equals(m)) {
                status = HttpStatus.BAD_REQUEST;        // 400
            } else if ("이미 발급된 사용자입니다.".equals(m)) {
                status = HttpStatus.CONFLICT;           // 409
            } else if ("선착순 마감되었습니다.".equals(m)) {
                status = HttpStatus.GONE;               // 410
            } else {
                status = HttpStatus.BAD_GATEWAY;        // 502 (UNKNOWN_ERROR 등)
            }
        }

        return ResponseEntity.status(status).body(response);
    }
}


