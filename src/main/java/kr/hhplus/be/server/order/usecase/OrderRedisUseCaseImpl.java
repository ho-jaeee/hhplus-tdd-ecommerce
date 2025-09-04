package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.couponRedis.domain.service.CouponCheckService;
import kr.hhplus.be.server.order.domain.service.OrderTransactionServiceImpl;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import kr.hhplus.be.server.order.usecase.lock.OrderLockKeyResolver;

import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderRedisUseCaseImpl implements OrderRedisUseCase {


    private final OrderTransactionServiceImpl orderTransactionService;
    private final ProductCheckService productCheckService;
    private final CouponCheckService couponCheckService;


    @Override
    @DistributedLock(
            resolver = OrderLockKeyResolver.class,
            waitMs = 5000,
            leaseMs = 0,
            sortKeys = true,
            throwOnTimeout = true
    )
    public OrderResult createOrder(OrderCommand command) {

        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();

        // 0. 재고 수량 확인
        productCheckService.validateAllStock(items);
        // 0. 쿠폰 유효성 검사
        couponCheckService.checkIfValidCouponNullable(userId, couponId);
        // 유스케이스는 락만 함
        return orderTransactionService.execute(command);
    }
}

