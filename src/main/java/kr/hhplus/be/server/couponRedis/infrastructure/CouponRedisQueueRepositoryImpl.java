package kr.hhplus.be.server.couponRedis.infrastructure;

import kr.hhplus.be.server.couponRedis.domain.repository.CouponRedisQueueRepository;
import kr.hhplus.be.server.couponRedis.domain.service.dto.FinishResult;
import kr.hhplus.be.server.couponRedis.domain.service.dto.HoldResult;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponRedisQueueRepositoryImpl implements CouponRedisQueueRepository {

    private final RedissonClient redisson;

    // ---- Redis Key helpers (해시태그 고정으로 동일 슬롯 유도) ----
    private static String kSeq(Long cId)   { return "coupon:{" + cId + "}:seq"; }
    private static String kQueue(Long cId) { return "coupon:{" + cId + "}:queue"; }
    private static String kIssued(Long cId){ return "coupon:{" + cId + "}:issued"; }
    private static String kResv(Long cId)  { return "coupon:{" + cId + "}:reservations"; }


    private static final String ENTER_LUA = """
    -- 이미 최종 발급된 사용자면 즉시 차단
    if redis.call('SISMEMBER', KEYS[3], ARGV[1]) == 1 then
    return { 'ERR', 'ALREADY_ISSUED' }
    end

    -- 멱등: 같은 reqId로 예약이 이미 존재하는가?
    if redis.call('HEXISTS', KEYS[4], ARGV[2]) == 1 then
    local r = redis.call('ZRANK', KEYS[2], ARGV[1])
    if not r then return { 'ERR', 'NOT_ENQUEUED' } end
        if r < tonumber(ARGV[3]) then
            return { 'HOLD', tostring(r) }
        else
            return { 'ERR', 'OUT_OF_CUT' }
        end
    end

    -- 순번 부여 & 큐 진입(NX)
    local seq = redis.call('INCR', KEYS[1])
    local added = redis.call('ZADD', KEYS[2], 'NX', seq, ARGV[1])
    if added == 0 then

    -- 이미 큐에 있음: 현재 랭크로 판정
    local r = redis.call('ZRANK', KEYS[2], ARGV[1])
    if not r then return { 'ERR', 'NOT_ENQUEUED' } end
    if r < tonumber(ARGV[3]) then
        return { 'HOLD', tostring(r) }
    else
        -- 컷 밖이면 자기 자신만 제거
        redis.call('ZREM', KEYS[2], ARGV[1])
        return { 'ERR', 'OUT_OF_CUT' }
        end
    end

    -- 새 진입: 컷 판정
    local rank = redis.call('ZRANK', KEYS[2], ARGV[1])
    if rank >= tonumber(ARGV[3]) then
        redis.call('ZREM', KEYS[2], ARGV[1])
        return { 'ERR', 'OUT_OF_CUT' }
    end

    -- 예약 생성(reqId -> expireAtMillis)
    local expireAt = tonumber(ARGV[4]) + tonumber(ARGV[5])
    redis.call('HSET', KEYS[4], ARGV[2], tostring(expireAt))
    return { 'HOLD', tostring(rank) }
    """;


    @Override
    public HoldResult enqueueAndHold(Long couponId, Long userId, String reqId, long limit, long nowMillis, long ttlMillis) {
        // Lua 실행
        List<Object> out = redisson.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                ENTER_LUA,
                RScript.ReturnType.MULTI,
                Arrays.asList(
                        kSeq(couponId),
                        kQueue(couponId),
                        kIssued(couponId),
                        kResv(couponId)
                ),
                String.valueOf(userId),
                reqId,
                String.valueOf(limit),
                String.valueOf(nowMillis),
                String.valueOf(ttlMillis)
        );

        // 결과 파싱: ["HOLD", rank] | ["ERR", code]
        String tag = (String) out.get(0);
        if ("HOLD".equals(tag)) {
            long rank = Long.parseLong((String) out.get(1));
            // HoldResult 생성 방식은 네 DTO에 맞춰 사용
            return new HoldResult(true, rank, null);
            // 또는 팩토리 메서드가 있다면: HoldResult.success(rank)
        } else { // "ERR"
            String code = (String) out.get(1); // ALREADY_ISSUED | OUT_OF_CUT | NOT_ENQUEUED
            return new HoldResult(false, null, code);
            // 또는: HoldResult.error(code)
        }

    }


    private static final String FINISH_LUA = """
     -- 1. 예약 존재 여부 확인
     local exp = redis.call('HGET', KEYS[3], ARGV[2])
     if not exp then
        return {'ERR','NO_RESERVATION'}
     end
     
     -- 2. 예약 만료 확인
    if tonumber(exp) < tonumber(ARGV[4]) then
        redis.call('ZREM', KEYS[1], ARGV[1])
        redis.call('HDEL', KEYS[3], ARGV[2])
        return {'ERR','RESERVATION_EXPIRED'}
    end
    
    -- 3. COMMIT 처리
    if ARGV[3] == 'COMMIT' then
        redis.call('SADD', KEYS[2], ARGV[1])
        redis.call('ZREM', KEYS[1], ARGV[1])
        redis.call('HDEL', KEYS[3], ARGV[2])
        return {'OK'}
        
    -- 4. CANCEL 처리
    else
        redis.call('ZREM', KEYS[1], ARGV[1])
        redis.call('HDEL', KEYS[3], ARGV[2])
        return {'CANCELED'}
    end
""";


    @Override
    public FinishResult finish(Long couponId, Long userId, String reqId, boolean commit, long nowMillis) {
        List<Object> out = redisson.getScript(StringCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                FINISH_LUA,
                RScript.ReturnType.MULTI,
                Arrays.asList(
                        kQueue(couponId),
                        kIssued(couponId),
                        kResv(couponId)
                ),
                String.valueOf(userId),
                reqId,
                commit ? "COMMIT" : "CANCEL",
                String.valueOf(nowMillis)
        );

        String tag = (String) out.get(0);
        if ("OK".equals(tag))       return FinishResult.ofOk();
        if ("CANCELED".equals(tag)) return FinishResult.ofCanceled();
        // ERR: NO_RESERVATION | RESERVATION_EXPIRED
        return FinishResult.ofError((String) out.get(1));
    }

}
