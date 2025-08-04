package kr.hhplus.be.server.mock.mockCoupon;

public record couponRespone(
        String couponName,
        Long couponId,
        Long userId,
        boolean used,
        String issuedAtOrMessage  // 발급 시간 or 실패 메시지
) {}