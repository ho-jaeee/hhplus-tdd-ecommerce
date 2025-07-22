package kr.hhplus.be.server.pointTest;

import kr.hhplus.be.server.point.controller.PointChargeController;
import kr.hhplus.be.server.point.controller.dto.PointChargeRequest;
import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.usecase.PointChargeUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PointChargeControllerTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("PATCH /point/{id}/charge - 포인트 충전 성공")
    void chargePoint() throws Exception {
        // given
        long userId = 1L;
        long point = 5000L;

        PointChargeRequest request = new PointChargeRequest(point);

        PointChargeUseCase mockUseCase = Mockito.mock(PointChargeUseCase.class);
        PointResponse mockedResponse = new PointResponse(userId, 15000L, System.currentTimeMillis());

        when(mockUseCase.ChargeUseCase(userId, point)).thenReturn(mockedResponse);

        PointChargeController controller = new PointChargeController(mockUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // when & then
        mockMvc.perform(patch("/point/charge/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(15000L));
    }


}
