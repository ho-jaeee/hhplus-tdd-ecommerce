package kr.hhplus.be.server.config.redisson;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Profile("!test")
@Configuration
public class RedissonConfig {
    @Value("${redisson.config}")
    private String redissonConfig;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() throws IOException {
        Config config = Config.fromYAML(new java.io.StringReader(redissonConfig));
        return Redisson.create(config);
    }
}
