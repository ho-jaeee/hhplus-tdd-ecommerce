package kr.hhplus.be.server;

public record couponRespone(
        Long couponId,
        Long userId,
        boolean used,
        String issuedAtOrMessage  // 발급 시간 or 실패 메시지
) {}