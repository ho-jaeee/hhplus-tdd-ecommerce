package kr.hhplus.be.server.couponRedisKafka.port;

public interface CouponUserRepositoryPort {
    /** idemKey(=requestId)로 멱등 보장하며 저장. 이미 존재하면 기존 PK 반환 */
    Long saveIssued(Long couponId, Long userId, String idemKey);
}
