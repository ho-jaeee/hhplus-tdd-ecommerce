package kr.hhplus.be.server.couponRedisKafka.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


/**
 쿠폰 사용보단 선착순 쿠폰 발급 학습이므로
 최소한의 컬럼 값을 사용함
**/
@Entity
@Table(name = "couponNew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용
@AllArgsConstructor
@Builder
public class CouponNewJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long total; //쿠폰 총수량

    @Column(nullable=false)
    private Long issued; //쿠폰 발급 수량

    private LocalDateTime createdAt; //쿠폰 생성일시

    private LocalDateTime updatedAt; //쿠폰 수정일시

}
