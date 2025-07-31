package kr.hhplus.be.server.integrationTest.couponTest;


import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.model.CouponUserJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.coupon.domain.service.CouponIssuedService;
import kr.hhplus.be.server.coupon.domain.service.dto.CouponUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CouponIssuedServiceTestInte {

    @Autowired
    private CouponIssuedService couponIssuedService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    private long couponId;
    private long userId;

    @BeforeEach
    void setUp() {
        //given
        // 기본 테스트용 쿠폰 저장
        CouponJPA couponJPA = couponRepository.save(new CouponJPA(
                null,
                "10% 할인쿠폰",
                10,
                100,
                10,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        couponId = couponJPA.getCouponId();
        userId = 2L;
    }

    @Test
    @DisplayName("정상적인 쿠폰 발급 통합 테스트")
    void couponIssuedIntegration() {

        // when
        CouponUser issued = couponIssuedService.issueCouponToUser(couponId, userId);

        // then
        assertThat(issued).isNotNull();
        assertThat(issued.getUserId()).isEqualTo(userId);
        assertThat(issued.getCouponId()).isEqualTo(couponId);
        assertThat(issued.isUsed()).isFalse();

        CouponUserJPA saved = couponUserRepository.findByUserIdAndCouponId(userId, couponId).orElseThrow();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getCouponId()).isEqualTo(couponId);
    }

}
