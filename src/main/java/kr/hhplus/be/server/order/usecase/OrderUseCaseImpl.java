package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.order.domain.service.OrderTransactionServiceImpl;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final ProductCheckService productCheckService;
    private final CouponCheckService couponCheckService;
    private final OrderTransactionServiceImpl orderTransactionService;
    private final RedissonClient redisson;

    @Override
    public OrderResult createOrder(OrderCommand command) {
        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();

        // --- 1) 멀티락 키 구성: [쿠폰] -> [포인트] -> [상품들(정렬)] ---
        List<String> keys = new ArrayList<>();
        if (couponId != null) keys.add("lock:coupon:{" + couponId + "}");
        keys.add("lock:point:{" + userId + "}");
        items.stream()
                .map(i -> "lock:product:{" + i.productId() + "}")
                .sorted(Comparator.naturalOrder())
                .forEach(keys::add);

        RLock[] locks = keys.stream().map(redisson::getLock).toArray(RLock[]::new);
        RedissonMultiLock multiLock = new RedissonMultiLock(locks);

        boolean locked = false;
        try{
            // waitTime=2s (필요시 조정). leaseTime 미지정 → 워치독 자동 연장(기본 30s)
            locked = multiLock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) throw new IllegalStateException("잠시 후 다시 시도해주세요.");

            // 1. 재고 수량 확인 (읽기-only → 트랜잭션 필요 없음)
            productCheckService.validateAllStock(items);
            // 2. 쿠폰 유효성 검사 (nullable, 읽기-only → 트랜잭션 필요 없음)
            couponCheckService.checkIfValidCouponNullable(userId, couponId);
            // 3. 실제 주문 생성 로직은 트랜잭션 포함된 서비스에 위임
            return orderTransactionService.execute(command);

        }catch (InterruptedException ie){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 대기 중 인터럽트", ie);
        }
        finally {
            if (locked) {
                try {
                    multiLock.unlock();
                } catch (Exception ignore) {
                }
            }

        }
    }
}


