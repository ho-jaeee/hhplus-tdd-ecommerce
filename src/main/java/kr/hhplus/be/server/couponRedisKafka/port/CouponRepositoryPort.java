package kr.hhplus.be.server.couponRedisKafka.port;

public interface CouponRepositoryPort {
    boolean existsById(Long couponId);


    /** 재고 1개 소진. total > issued 인 경우에만 issued=issued+1 성공 → true */
    boolean tryIncreaseIssued(Long couponId);
}
