package kr.hhplus.be.server.integrationTest.couponTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.coupon.controller.dto.CouponRequest;
import kr.hhplus.be.server.coupon.domain.model.CouponJPA;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.coupon.domain.repository.CouponUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CouponControllerTestInte {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long couponId;
    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 2L;
        CouponJPA saved = couponRepository.save(new CouponJPA(
                null,
                "10% 할인",
                10,
                100,
                0,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        couponId = saved.getCouponId();
    }

    @Test
    @DisplayName("쿠폰 발급 API가 정상적으로 작동한다")
    void issueCoupon_success() throws Exception {
        // given
        CouponRequest request = new CouponRequest(userId, couponId);
        String json = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.couponId").value(couponId))
                .andExpect(jsonPath("$.message").value("쿠폰 발급 성공"));
    }

}
