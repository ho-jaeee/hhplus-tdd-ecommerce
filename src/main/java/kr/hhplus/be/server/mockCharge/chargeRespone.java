package kr.hhplus.be.server.mockCharge;


public record chargeRespone(
        Long userId,

        Long point,

        Long updateMillis
) {}