package kr.hhplus.be.server.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.StringReader;

@TestConfiguration
@Profile("test")
public class RedissonTestConfig {
    @Bean(destroyMethod = "shutdown")
    @Primary
    public RedissonClient redissonClient(@Value("${redisson.config}") String yaml) throws Exception {
        return Redisson.create(Config.fromYAML(new StringReader(yaml)));
    }
}