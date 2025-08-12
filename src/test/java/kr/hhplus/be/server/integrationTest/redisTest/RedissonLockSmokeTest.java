package kr.hhplus.be.server.integrationTest.redisTest;

import kr.hhplus.be.server.config.RedissonTestConfig;
import org.junit.jupiter.api.Test;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(RedissonTestConfig.class)
public class RedissonLockSmokeTest {

    @Autowired
    RedissonClient redisson;

    @Test
    void lock_basic_acquire_and_release() throws Exception {
        String key = "lock:{product}:1001"; // 해시태그로 슬롯 고정 → 특정 마스터에서 처리
        RLock lock = redisson.getLock(key);

        boolean ok = lock.tryLock(300, TimeUnit.MILLISECONDS); // wait만 지정 → watchdog ON
        assertThat(ok).isTrue();
        try {
            // 임계구간
            assertThat(lock.isLocked()).isTrue();
            assertThat(lock.isHeldByCurrentThread()).isTrue();
        } finally {
            lock.unlock(); // 해제
        }
    }
}
