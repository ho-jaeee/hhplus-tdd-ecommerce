package kr.hhplus.be.server.couponRedisKafka.controller;


import kr.hhplus.be.server.couponRedisKafka.usecase.CouponIssuedUseCaseNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueCommandNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.dto.CouponIssueResultNew;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/new/coupons")
@RequiredArgsConstructor
public class CouponNewController {

    private final CouponIssuedUseCaseNew useCase;


    /**
     * 선착순 발급 요청 (동기 구간: 접수 + rank 반환)
     * 예) POST /api/new/coupons/{couponId}/issue?userId=1&requestId=REQ-1
     */
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<CouponIssueResultNew> issue(
            @PathVariable Long couponId,
            @RequestParam Long userId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant requestedAt
    ) {
        String rid = (requestId == null || requestId.isBlank())
                ? UUID.randomUUID().toString()
                : requestId;
        Instant occurredAt = (requestedAt != null) ? requestedAt : Instant.now();

        CouponIssueCommandNew cmd = new CouponIssueCommandNew(couponId, userId, rid, occurredAt);
        CouponIssueResultNew result = useCase.issue(cmd);
        return ResponseEntity.ok(result);
    }
}
