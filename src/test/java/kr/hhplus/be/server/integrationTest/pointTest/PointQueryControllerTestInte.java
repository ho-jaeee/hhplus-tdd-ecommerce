package kr.hhplus.be.server.integrationTest.pointTest;

import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PointQueryControllerTestInte {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointRepository userPointRepository;

    private final long userId = 1L;

    @BeforeEach
    void setup() {
        // 테스트용 유저 포인트 초기 세팅
        userPointRepository.deleteAll();
        userPointRepository.save(new UserPointJPA(userId, 10000L, System.currentTimeMillis()));
    }

    @Test
    @DisplayName("GET /point/{id} - 포인트 조회 성공")
    void getPointIntegrationTest() throws Exception {
        // API 호출 및 응답 검증
        mockMvc.perform(get("/point/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(10000L));

        // DB 직접 조회로 값 검증
        UserPointJPA userPoint = userPointRepository.findById(userId).orElseThrow();
        assertThat(userPoint.getPoint()).isEqualTo(10000L);
    }
}
