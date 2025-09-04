package kr.hhplus.be.server.couponRedisKafka.config;

import kr.hhplus.be.server.couponRedisKafka.port.CouponRepositoryPort;
import kr.hhplus.be.server.couponRedisKafka.port.EventBusPort;
import kr.hhplus.be.server.couponRedisKafka.port.GatekeeperPort;
import kr.hhplus.be.server.couponRedisKafka.usecase.CouponIssuedUseCaseNew;
import kr.hhplus.be.server.couponRedisKafka.usecase.CouponIssuedUseCaseNewImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponIssueWiringConfig {

    @Bean
    public CouponIssuedUseCaseNew issueCouponUseCaseNew(
            CouponRepositoryPort couponRepositoryPort,
            GatekeeperPort gatekeeperPort,
            EventBusPort eventBusPort
    ) {
        return new CouponIssuedUseCaseNewImpl(couponRepositoryPort, gatekeeperPort, eventBusPort);
    }

}
