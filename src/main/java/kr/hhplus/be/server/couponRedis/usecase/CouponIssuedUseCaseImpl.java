package kr.hhplus.be.server.couponRedis.usecase;



import kr.hhplus.be.server.couponRedis.domain.service.CouponIssuedService;
import kr.hhplus.be.server.couponRedis.domain.service.dto.CouponUser;
import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueCommand;
import kr.hhplus.be.server.couponRedis.usecase.dto.CouponIssueResult;
import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponIssuedUseCaseImpl implements CouponIssuedUseCase {

    private final CouponIssuedService couponIssuedService;


    @Override
    public CouponIssueResult issueCoupon(CouponIssueCommand command) {
        Long couponId = command.couponId();
        Long userId   = command.userId();

        if (couponId == null || couponId <= 0 || userId == null || userId <= 0) {
            return CouponIssueResult.fail(userId, couponId, "잘못된 입력입니다.");
        }

        String reqId = (command.reqId() == null || command.reqId().isBlank())
                ? UUID.randomUUID().toString()
                : command.reqId();

        try {
            // 서비스는 최종적으로 DB 확정까지 수행
            CouponUser issued = couponIssuedService.issueCouponToUser(couponId, userId, reqId);

            // rank를 내려주고 싶으면 service에서 HoldResult.rank를 반환 경로에 포함시키거나,
            // Redis enqueue 시점의 rank를 UseCase에서 별도로 받도록 확장 필요.
            // 여기서는 rank 없이 OK만 반환.
            return CouponIssueResult.ok(userId, couponId, null);

        } catch (IllegalArgumentException e) {
            // 서비스에서 던진 사용자 오류 메시지 그대로 전달
            return CouponIssueResult.fail(userId, couponId, e.getMessage());
        } catch (Exception e) {
            return CouponIssueResult.fail(userId, couponId, "UNKNOWN_ERROR");
        }
    }
}