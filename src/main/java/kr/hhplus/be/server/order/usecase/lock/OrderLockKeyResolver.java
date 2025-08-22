package kr.hhplus.be.server.order.usecase.lock;


import kr.hhplus.be.server.common.lock.LockKeyResolver;
import kr.hhplus.be.server.order.usecase.dto.OrderCommand;
import kr.hhplus.be.server.order.usecase.dto.OrderItemCommand;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.*;

public class OrderLockKeyResolver implements LockKeyResolver {

    @Override
    public List<String> resolveKeys(ProceedingJoinPoint pjp) {
        OrderCommand cmd = (OrderCommand) pjp.getArgs()[0];

        Long userId   = cmd.userId();
        Long couponId = cmd.couponId();

        // LinkedHashSet: 중복 제거 + 추가 순서 유지
        Set<String> keys = new LinkedHashSet<>();

        // 1) 사용자(포인트/한 사용자 동시 주문 충돌 방지)
        if (userId != null) keys.add("lock:user:" + userId);

        // 2) 쿠폰(옵션: 단건 사용 보장)
        if (couponId != null) keys.add("lock:coupon:" + couponId);

        // 3) 상품들(정렬하여 교착 방지)
        cmd.items().stream()
                .map(OrderItemCommand::productId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .forEach(pid -> keys.add("lock:product:" + pid));

        return new ArrayList<>(keys);
    }
}