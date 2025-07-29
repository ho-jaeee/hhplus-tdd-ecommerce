package kr.hhplus.be.server.coupon.usecase;

import kr.hhplus.be.server.coupon.domain.service.CouponUser;
import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CouponIssuedUseCaseImpl implements CouponIssuedUseCase {

    private final CouponIssuedService couponIssuedService;

    @Override
    public CouponIssueResult issueCoupon(CouponIssueCommand command) {
        Long couponId = command.couponId();
        Long userId = command.userId();

        // 도메인 서비스 호출
        CouponUser couponUser = couponIssuedService.issueCouponToUser(couponId, userId);
        Optional<CouponUser> result = Optional.ofNullable(couponUser);

        // 성공 or 실패 응답 생성
        return result
                .map(CouponUser -> CouponIssueResult.success(userId, couponId))
                .orElseGet(() -> CouponIssueResult.fail(userId, couponId, "쿠폰 발급 실패 또는 이미 발급됨"));
    }
}
