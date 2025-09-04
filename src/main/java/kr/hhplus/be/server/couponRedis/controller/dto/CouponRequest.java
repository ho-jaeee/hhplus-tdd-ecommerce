package kr.hhplus.be.server.couponRedis.controller.dto;

public record CouponRequest (
        Long userId,
        Long couponId,
        String reqId
){
    public boolean hasReqId() {
        return reqId != null && !reqId.isBlank();
    }
}
