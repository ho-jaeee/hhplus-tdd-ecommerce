package kr.hhplus.be.server.integrationTest.pointTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.point.controller.dto.PointChargeRequest;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PointChargeControllerTestIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointRepository userPointRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final long userId = 1L;

    @BeforeEach
    void setup() {
        // DB에 테스트용 유저 초기화 (포인트 10000으로 세팅)
        userPointRepository.deleteAll(); // 기존 데이터 클리어
        userPointRepository.save(new UserPointJPA(userId, 10000L, System.currentTimeMillis()));
    }

    @Test
    @DisplayName("PATCH /point/charge/{id} - 포인트 충전 성공")
    void chargePointIntegrationTest() throws Exception {
        long chargeAmount = 5000L;
        PointChargeRequest request = new PointChargeRequest(chargeAmount);

        // API 호출
        mockMvc.perform(patch("/point/charge/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // DB에서 결과 확인
        UserPointJPA updatedUserPoint = userPointRepository.findById(userId);
        assertThat(updatedUserPoint.getPoint()).isEqualTo(15000L);
    }
}
