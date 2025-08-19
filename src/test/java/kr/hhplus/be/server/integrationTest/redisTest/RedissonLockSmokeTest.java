package kr.hhplus.be.server.integrationTest.redisTest;

import kr.hhplus.be.server.config.RedissonTestConfig;
import org.junit.jupiter.api.Test;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedissonTestConfig.class)
@ActiveProfiles("test")
public class RedissonLockSmokeTest {

    @Autowired
    RedissonClient redisson;

    @Test
    void lock_basic_acquire_and_release() throws Exception {

        // 같은 슬롯 고정(선택): {product} 해시태그
        String key = "lock:{product}:1001";
        RLock lock = redisson.getLock(key);

        // wait=300ms, lease=2s (watchdog 의존 최소화)
        boolean ok = lock.tryLock(5, 5, TimeUnit.SECONDS);
        assertThat(ok).as("첫 락 획득").isTrue();

        try {
            assertThat(lock.isLocked()).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isTrue();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        // 해제 후 재획득 가능해야 함
        boolean ok2 = lock.tryLock(5, 5, TimeUnit.SECONDS);
        assertThat(ok2).as("해제 후 재락 획득").isTrue();
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
