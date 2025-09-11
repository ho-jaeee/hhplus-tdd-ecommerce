package kr.hhplus.be.server.couponRedisKafka.usecase.dto;

public enum IssueResultCode {
    SUCCESS, OUT_OF_STOCK, IDEMPOTENT_HIT, INVALID_COUPON, ERROR, INVALID_ARGUMENT
}
