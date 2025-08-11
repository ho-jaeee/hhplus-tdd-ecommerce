package kr.hhplus.be.server.lockCoordinator;



import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LockCoordinator {
    // 리포지토리들은 내부에서 @Lock(PESSIMISTIC_WRITE) or for update를 사용
    private final CouponUserRepository couponUserRepo;
    private final UserPointRepository userPointRepo;
    private final ProductRepository productRepo;

    public void lockInOrder(Long couponId, Long userId, List<Long> productIds) {
        // 1) Coupon
        if (couponId != null) {
            couponUserRepo.findByUserIdAndCouponIdForUpdate(userId, couponId)
                    .orElseThrow(() -> new IllegalArgumentException("쿠폰 없음"));
        }
        // 2) UserPoint
        userPointRepo.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 없음"));

        // 3) Product (ID 오름차순)
        productIds.stream().sorted().forEach(pid -> {
            productRepo.findById(pid)
                    .orElseThrow(() -> new IllegalArgumentException("상품 없음"));
        });
    }
}
