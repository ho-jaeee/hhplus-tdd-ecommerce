package kr.hhplus.be.server.unittest.couponTest;

import kr.hhplus.be.server.couponRedis.domain.model.CouponJPA;
import kr.hhplus.be.server.couponRedis.domain.model.CouponUserJPA;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRedisQueueRepository;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponRepository;
import kr.hhplus.be.server.couponRedis.domain.repository.CouponUserRepository;
import kr.hhplus.be.server.couponRedis.domain.service.CouponIssuedService;

import kr.hhplus.be.server.couponRedis.domain.service.dto.CouponUser;

import kr.hhplus.be.server.couponRedis.domain.service.dto.FinishResult;
import kr.hhplus.be.server.couponRedis.domain.service.dto.HoldResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CouponIssuedServiceTest {

    @Mock CouponRepository couponRepo;
    @Mock CouponUserRepository couponUserRepo;
    @Mock
    CouponRedisQueueRepository redisRepo;

    @InjectMocks CouponIssuedService sut;

    private CouponJPA coupon(Long id, int total, int issued) {
        return CouponJPA.builder()
                .couponId(id).name("테스트쿠폰")
                .discountAmount(10)
                .totalQuantity(total)
                .issuedQuantity(issued)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @BeforeEach
    void setup() {}

    @Test
    @DisplayName("쿠폰 발급 성공 시 DB와 Redis에 정상 반영된다")
    void issue_success() {
        Long couponId = 1L, userId = 10L; String reqId = "r-1";
        when(couponRepo.findById(couponId)).thenReturn(Optional.of(coupon(couponId, 3, 0)));
        when(redisRepo.enqueueAndHold(eq(couponId), eq(userId), eq(reqId), anyLong(), anyLong(), anyLong()))
                .thenReturn(new HoldResult(true, 0L, null));

        CouponUserJPA saved = CouponUserJPA.builder()
                .id(100L).userId(userId).couponId(couponId).isUsed(false).issuedAt(LocalDateTime.now())
                .build();
        when(couponUserRepo.save(any())).thenReturn(saved);
        when(redisRepo.finish(eq(couponId), eq(userId), eq(reqId), eq(true), anyLong()))
                .thenReturn(FinishResult.ofOk());

        CouponUser result = sut.issueCouponToUser(couponId, userId, reqId);

        assertThat(result.getUserId()).isEqualTo(userId);
        verify(couponRepo).incrementIssuedQuantity(couponId);
        verify(redisRepo).finish(couponId, userId, reqId, true, anyLong());
    }

    @Test
    @DisplayName("이미 발급된 사용자가 다시 요청하면 예외 발생")
    void issue_fails_when_already_issued() {
        Long couponId = 1L, userId = 10L; String reqId = "r-1";
        when(couponRepo.findById(couponId)).thenReturn(Optional.of(coupon(couponId, 3, 0)));
        when(redisRepo.enqueueAndHold(eq(couponId), eq(userId), eq(reqId), anyLong(), anyLong(), anyLong()))
                .thenReturn(new HoldResult(false, null, "ALREADY_ISSUED"));

        assertThatThrownBy(() -> sut.issueCouponToUser(couponId, userId, reqId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 발급된");
        verifyNoInteractions(couponUserRepo);
    }

    @Test
    @DisplayName("선착순 마감된 경우 예외 발생")
    void issue_fails_when_out_of_cut() {
        Long couponId = 1L, userId = 10L; String reqId = "r-1";
        when(couponRepo.findById(couponId)).thenReturn(Optional.of(coupon(couponId, 1, 0)));
        when(redisRepo.enqueueAndHold(eq(couponId), eq(userId), eq(reqId), anyLong(), anyLong(), anyLong()))
                .thenReturn(new HoldResult(false, null, "OUT_OF_CUT"));

        assertThatThrownBy(() -> sut.issueCouponToUser(couponId, userId, reqId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("선착순 마감");
        verifyNoInteractions(couponUserRepo);
    }

    @Test
    @DisplayName("중복 키 예외 발생 시 멱등 처리되어 기존 발급 내역을 반환한다")
    void issue_idempotent_on_duplicateKey() {
        Long couponId = 1L, userId = 10L; String reqId = "r-dup";
        when(couponRepo.findById(couponId)).thenReturn(Optional.of(coupon(couponId, 5, 0)));
        when(redisRepo.enqueueAndHold(eq(couponId), eq(userId), eq(reqId), anyLong(), anyLong(), anyLong()))
                .thenReturn(new HoldResult(true, 0L, null));
        when(couponUserRepo.save(any())).thenThrow(new DataIntegrityViolationException("dup unique"));

        CouponUserJPA existing = CouponUserJPA.builder().id(7L).userId(userId).couponId(couponId).build();
        when(couponUserRepo.findByCouponIdAndReqId(couponId, reqId)).thenReturn(Optional.of(existing));
        when(redisRepo.finish(eq(couponId), eq(userId), eq(reqId), eq(true), anyLong()))
                .thenReturn(FinishResult.ofOk());

        CouponUser result = sut.issueCouponToUser(couponId, userId, reqId);

        assertThat(result.getUserId()).isEqualTo(userId);
        verify(redisRepo).finish(couponId, userId, reqId, true, anyLong());
    }

    @Test
    @DisplayName("DB 오류 발생 시 Redis 예약이 취소된다")
    void issue_rolls_back_and_cancel_on_runtime_exception() {
        Long couponId = 1L, userId = 10L; String reqId = "r-x";
        when(couponRepo.findById(couponId)).thenReturn(Optional.of(coupon(couponId, 5, 0)));
        when(redisRepo.enqueueAndHold(eq(couponId), eq(userId), eq(reqId), anyLong(), anyLong(), anyLong()))
                .thenReturn(new HoldResult(true, 0L, null));
        when(couponUserRepo.save(any())).thenThrow(new RuntimeException("db down"));

        assertThatThrownBy(() -> sut.issueCouponToUser(couponId, userId, reqId))
                .isInstanceOf(RuntimeException.class);

        verify(redisRepo).finish(couponId, userId, reqId, false, anyLong());
    }

}
