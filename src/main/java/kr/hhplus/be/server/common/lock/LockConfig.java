package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class LockConfig {
    private final RedissonClient redissonClient;

    @Bean
    public DistributedLockAspect distributedLockAspect(RedissonClient redissonClient,  ApplicationContext applicationContext) {
        return new DistributedLockAspect(redissonClient, applicationContext);
    }
}