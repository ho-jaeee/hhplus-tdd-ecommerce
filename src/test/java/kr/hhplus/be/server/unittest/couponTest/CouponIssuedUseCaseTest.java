package kr.hhplus.be.server.unittest.couponTest;

import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import kr.hhplus.be.server.coupon.usecase.CouponIssueCommand;
import kr.hhplus.be.server.coupon.usecase.CouponIssueResult;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCase;
import kr.hhplus.be.server.coupon.usecase.CouponIssuedUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CouponIssuedUseCaseTest {

    @Mock
    private CouponIssuedService couponIssuedService;

    @Mock
    private RedissonClient redisson;

    @Mock
    private RLock lock;

    private CouponIssuedUseCase useCase;

    @BeforeEach
    void setUp() throws Exception {
        // SUT 생성자 시그니처: (CouponIssuedService, RedissonClient)
        useCase = new CouponIssuedUseCaseImpl(couponIssuedService, redisson);

        // 레디슨 락 목 세팅
        given(redisson.getLock(anyString())).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
        // finally에서 unlock 경로 타도록
        given(lock.isHeldByCurrentThread()).willReturn(true);
    }

    @Test
    @DisplayName("쿠폰이 정상적으로 발급된다.")
    void issueCoupon_success() {
        // given
        long couponId = 100L;
        long userId = 2L;

        CouponUser couponUser = new CouponUser(
                userId,
                couponId,
                false,
                LocalDateTime.now(),
                null
        );

        given(couponIssuedService.issueCouponToUser(couponId, userId))
                .willReturn(couponUser);

        CouponIssueCommand command = new CouponIssueCommand(userId, couponId);

        // when
        CouponIssueResult actual = useCase.issueCoupon(command);

        // then
        then(couponIssuedService).should(times(1))
                .issueCouponToUser(couponId, userId);

        assertThat(actual.message()).isEqualTo("쿠폰 발급 성공");
        assertThat(actual.userId()).isEqualTo(userId);
        assertThat(actual.couponId()).isEqualTo(couponId);
    }
}