package kr.hhplus.be.server;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import jakarta.annotation.PreDestroy;

import org.junit.jupiter.api.TestInstance;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import org.testcontainers.utility.DockerImageName;



@Configuration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestcontainersConfiguration {

	public static final MySQLContainer<?> MYSQL_CONTAINER;
    public static final GenericContainer<?> REDIS_CONTAINER;
    public static final ConfluentKafkaContainer KAFKA_CONTAINER;



	static {
        //MySQL
		MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
			.withDatabaseName("hhplus")
			.withUsername("test")
			.withPassword("test");
		MYSQL_CONTAINER.start();

		System.setProperty("spring.datasource.url", MYSQL_CONTAINER.getJdbcUrl() + "?characterEncoding=UTF-8&serverTimezone=UTC");
		System.setProperty("spring.datasource.username", MYSQL_CONTAINER.getUsername());
		System.setProperty("spring.datasource.password", MYSQL_CONTAINER.getPassword());

        // Redis
        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(6379);
        REDIS_CONTAINER.start();

        String redisHost = REDIS_CONTAINER.getHost();
        Integer redisPort = REDIS_CONTAINER.getMappedPort(6379);

        // Spring Data Redis 기본 프로퍼티
        System.setProperty("spring.data.redis.host", redisHost);
        System.setProperty("spring.data.redis.port", String.valueOf(redisPort));

        // 호환성(환경에 따라 사용하는 키가 다를 경우 대비)
        System.setProperty("spring.redis.host", redisHost);
        System.setProperty("spring.redis.port", String.valueOf(redisPort));

        // URL 형태가 필요한 경우(예: 일부 Redisson 설정 등)
        System.setProperty("spring.data.redis.url", "redis://" + redisHost + ":" + redisPort);


        // Kafka (KRaft 모드, ZK 불필요)
//        KAFKA_CONTAINER = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.1")
//                // 토픽 자동생성 비활성화(원하면 유지/변경)
//                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false")
//                .withStartupTimeout(java.time.Duration.ofMinutes(2));

        KAFKA_CONTAINER = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.6.1")
                .withExposedPorts(9092) // 컨테이너 9092 노출
                .withCreateContainerCmdModifier(cmd ->
                        cmd.getHostConfig().withPortBindings(
                                new PortBinding(Ports.Binding.bindPort(9092), new ExposedPort(9092))
                        )
                );

        KAFKA_CONTAINER.start();

        // Spring Kafka 부트스트랩 서버 주입
        System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());

        // (선택) JSON 역직렬화 시 타입 정보/신뢰 패키지 설정
        System.setProperty("spring.kafka.consumer.properties.spring.json.trusted.packages", "*");
        System.setProperty("spring.kafka.consumer.properties.spring.json.use.type.headers", "false");


	}

	@PreDestroy
	public void preDestroy() {
		if (MYSQL_CONTAINER.isRunning()) {
			MYSQL_CONTAINER.stop();
		}
        if (REDIS_CONTAINER.isRunning()) {
            REDIS_CONTAINER.stop();
        }
        if (KAFKA_CONTAINER.isRunning()) {
            KAFKA_CONTAINER.stop();
        }
	}
}