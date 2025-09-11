package kr.hhplus.be.server.order.domain.service;


import kr.hhplus.be.server.order.domain.model.OrderKafkaJPA;
import kr.hhplus.be.server.order.domain.model.OrderKafkaItemJPA;
import kr.hhplus.be.server.order.domain.repository.OrderKafkaRepository;
import kr.hhplus.be.server.order.domain.repository.OrderKafkaItemRepository;
import kr.hhplus.be.server.order.kafka.dto.OrderPlacedKafka;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;


@Service
@RequiredArgsConstructor
public class OrderKafkaService {

    private final OrderKafkaRepository orderKafkaRepository;
    private final OrderKafkaItemRepository orderKafkaItemRepository;

    /**
     * Kafka DTO를 그대로 영속화한다.
     * - 헤더: save(entity) → 없으면 INSERT, 있으면 UPDATE (JPA Upsert)
     * - 아이템: 기존 전량 삭제 후 메시지 기준으로 재삽입 (멱등/단순)
     */
    @Transactional
    public void save(OrderPlacedKafka msg) {
        // 1) 헤더 Upsert
        OrderKafkaJPA header = orderKafkaRepository.findById(msg.orderId())
                .orElseGet(OrderKafkaJPA::new);

        header.setOrderId(msg.orderId());
        header.setUserId(msg.userId());
        header.setCouponId(msg.couponId());
        header.setTotalPrice(msg.totalPrice());
        header.setPayPoint(msg.payPoint());
        header.setCreatedAt(msg.createdAt());

        // 컬렉션 초기화(혹시 기존 프록시가 걸려 있을 수 있어 안전하게 교체)
        header.setItems(new ArrayList<>());

        // 2) 아이템 전량 재구성
        msg.items().forEach(it -> {
            OrderKafkaItemJPA row = new OrderKafkaItemJPA();
            row.setOrderEvent(header);                 // 연관관계 주인 설정
            row.setProductId(it.productId());
            row.setProductName(it.productName());
            row.setPricePerUnit(it.pricePerUnit());
            row.setQuantity(it.quantity());
            header.getItems().add(row);                // 편의 메서드 없이 직접 add
        });

        // 3) 저장 (Cascade.ALL이 아니면 아이템 별도 saveAll 필요)
        //    - 엔티티 매핑에서 OrderKafkaJPA.items에 Cascade.ALL이면 save(header) 한 번이면 끝.
        //    - Cascade가 없다면, 아이템 delete → saveAll 순서 수행.
        orderKafkaRepository.save(header);

        // Cascade 설정이 없다면 주석 해제:
        // orderKafkaItemRepository.deleteByOrderId(msg.orderId());
        // orderKafkaItemRepository.saveAll(header.getItems());
    }
}
