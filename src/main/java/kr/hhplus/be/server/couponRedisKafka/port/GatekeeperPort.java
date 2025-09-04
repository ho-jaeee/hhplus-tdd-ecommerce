package kr.hhplus.be.server.couponRedisKafka.port;

// Redis 게이트키퍼: 선착순 rank 부여(원자 증가)
public interface GatekeeperPort {
    /** 선착순 rank 부여. 구현은 Redis INCR/SEQ 등 */
    long assignRank(Long couponId, Long userId, String requestId);
    boolean gate(Long couponId, long rank);                 // 사전 허용 판단
    void enqueuePending(Long couponId, String requestId, long rank); // 보류 등록
    void advance(Long couponId, long count);                // 커밋 성공 후 next++
    long releaseContinuous(Long couponId);                  // 연속 구간 해제
}
