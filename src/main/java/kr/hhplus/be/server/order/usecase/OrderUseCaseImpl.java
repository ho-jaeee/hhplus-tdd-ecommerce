package kr.hhplus.be.server.order.usecase;

import kr.hhplus.be.server.coupon.domain.service.CouponCheckService;
import kr.hhplus.be.server.order.domain.service.OrderTransactionServiceImpl;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import kr.hhplus.be.server.product.domain.service.ProductCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderUseCaseImpl implements OrderUseCase {

    private final ProductCheckService productCheckService;
    private final CouponCheckService couponCheckService;
    private final OrderTransactionServiceImpl orderTransactionService;

    @Override
    // @Transactional
    public OrderResult createOrder(OrderCommand command) {
        Long userId = command.userId();
        Long couponId = command.couponId();
        List<OrderItemCommand> items = command.items();

        // 1. 재고 수량 확인 (읽기-only → 트랜잭션 필요 없음)
        productCheckService.validateAllStock(items);

        // 2. 쿠폰 유효성 검사 (nullable, 읽기-only → 트랜잭션 필요 없음)
        couponCheckService.checkIfValidCouponNullable(userId, couponId);

        // 3. 실제 주문 생성 로직은 트랜잭션 포함된 서비스에 위임
        return orderTransactionService.execute(command);


    }
}


