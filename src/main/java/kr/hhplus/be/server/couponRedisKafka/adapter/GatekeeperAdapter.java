package kr.hhplus.be.server.couponRedisKafka.adapter;

import kr.hhplus.be.server.couponRedisKafka.port.GatekeeperPort;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GatekeeperAdapter implements GatekeeperPort{

    private final RedissonClient redisson;


    private String kSeq(Long couponId){ return "coupon:v2:%d:seq".formatted(couponId); }
    private String kNext(Long couponId){ return "coupon:v2:%d:next".formatted(couponId); }
    private String kPend(Long couponId){ return "coupon:v2:%d:pending".formatted(couponId); }

    @Override
    public long assignRank(Long couponId, Long userId, String requestId) {
        RAtomicLong seq = redisson.getAtomicLong(kSeq(couponId));
        return seq.incrementAndGet(); // Redis에서 원자 증가
    }


    @Override public boolean gate(Long c, long rank) {
        long next = redisson.getAtomicLong(kNext(c)).get(); // 기본 0
        return rank == next + 1; // 엄격 FCFS (윈도우 쓰려면 <= next+W)
    }

    @Override public void enqueuePending(Long c, String requestId, long rank) {
        redisson.getScoredSortedSet(kPend(c)).add(rank, requestId);
    }

    @Override public void advance(Long c, long count) {
        redisson.getAtomicLong(kNext(c)).addAndGet(count); // 커밋 성공 건수만큼 증가
    }

    @Override public long releaseContinuous(Long c) {
        long next = redisson.getAtomicLong(kNext(c)).get();
        return redisson.getScoredSortedSet(kPend(c))
                .removeRangeByScore(0, true, next, true); // next 이하 일괄 해제
    }
}
