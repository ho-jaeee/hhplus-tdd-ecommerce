package kr.hhplus.be.server.integrationTest.productTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.config.RedissonTestConfig;
import kr.hhplus.be.server.product.domain.model.ProductPopularJPA;
import kr.hhplus.be.server.product.domain.repository.ProductPopularRepository;
import kr.hhplus.be.server.product.domain.service.ProductPopularSaveServiceImpl;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Import({TestcontainersConfiguration.class, RedissonTestConfig.class})
public class ProductPopularSaveTestIntegration {
    @Autowired
    ProductPopularSaveServiceImpl service;

    @Autowired
    ProductPopularRepository popularRepo;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    void clean() {
        popularRepo.deleteAll();
    }

    @Test
    @DisplayName("같은 시 버킷에서 score가 누적된다 있으면 update")
    void addSale_accumulates_in_same_hour_bucket() {
        long pid = 101L;
        LocalDateTime t = LocalDateTime.of(2025, 8, 20, 15, 12, 34);
        LocalDateTime bucket = t.truncatedTo(ChronoUnit.HOURS); // 2025-08-20T15:00:00

        service.addSale(pid, 2, t);
        service.addSale(pid, 3, t);

        em.clear();

        ProductPopularJPA row = popularRepo
                .findByProductIdAndBucketStart(pid, bucket)
                .orElseThrow();

        assertThat(row.getProductId()).isEqualTo(pid);
        assertThat(row.getBucketStart()).isEqualTo(bucket);
        assertThat(row.getScore()).isEqualTo(5L);
        assertThat(row.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 시간대면 다른 버킷으로 분리 저장된다 없으면 insert")
    void addSale_separates_rows_across_hour_boundaries() {
        long pid = 202L;
        LocalDateTime t1 = LocalDateTime.of(2025, 8, 20, 15, 59, 59);
        LocalDateTime t2 = LocalDateTime.of(2025, 8, 20, 16, 0, 1);

        var b1 = t1.truncatedTo(ChronoUnit.HOURS);
        var b2 = t2.truncatedTo(ChronoUnit.HOURS);

        service.addSale(pid, 1, t1);
        service.addSale(pid, 4, t2);


        em.clear();

        var r1 = popularRepo.findByProductIdAndBucketStart(pid, b1).orElseThrow();
        var r2 = popularRepo.findByProductIdAndBucketStart(pid, b2).orElseThrow();

        assertThat(r1.getScore()).isEqualTo(1L);
        assertThat(r2.getScore()).isEqualTo(4L);
    }

    @Test
    @DisplayName("음수 수량(환불/보정) 반영 시 누적 감소한다, 추후 보상을 위한")
    void addSale_supports_negative_adjustment() {
        long pid = 303L;
        LocalDateTime t = LocalDateTime.of(2025, 8, 20, 10, 10, 10);
        LocalDateTime bucket = t.truncatedTo(ChronoUnit.HOURS);

        service.addSale(pid, 5, t);
        service.addSale(pid, -2, t);


        em.clear();

        var row = popularRepo.findByProductIdAndBucketStart(pid, bucket).orElseThrow();
        assertThat(row.getScore()).isEqualTo(3L);
    }
}
