package kr.hhplus.be.server.couponRedisKafka.domain.Service;

public interface CouponIssueServiceNew {
    IssueAttemptResult CouponIssueIdempotent(Long couponId, Long userId, String idemKey);
    enum Reason { SUCCESS, OUT_OF_STOCK, IDEMPOTENT_HIT, ERROR }

    record IssueAttemptResult(boolean committed, Long couponUserId, Reason reason){}

}
