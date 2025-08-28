package kr.hhplus.be.server.product.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "product_popular_outbox",
        indexes = {
                @Index(name = "idx_outbox_status_id", columnList = "status,id"),
                @Index(name = "idx_outbox_event_time", columnList = "eventTime")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class ProductPopularOutboxJPA {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private long scoreDelta;

    private Instant eventTime;            // 이벤트가 발생한 시간(KST 기준 변환 후 UTC 저장 추천)

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;          // PENDING/PROCESSING/DONE/ERROR

    private int retryCount;
    private Instant nextRetryAt;          // 재시도 시각(백오프)

    @CreationTimestamp
    private Instant createdAt;

    @Version
    private Long version;                 // 낙관적 잠금(중복 처리 방지 보조)
}
