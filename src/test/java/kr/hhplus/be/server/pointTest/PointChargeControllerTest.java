package kr.hhplus.be.server.pointTest;

import kr.hhplus.be.server.point.controller.PointChargeController;
import kr.hhplus.be.server.point.controller.dto.PointChargeRequest;
import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.usecase.PointChargeUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PointChargeControllerTest {

    @Mock
    PointChargeUseCase pointChargeUseCase;

    MockMvc mockMvc;
    ObjectMapper objectMapper;


    @InjectMocks
    PointChargeController pointChargeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointChargeController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void ChargePointTest() throws Exception {

        // Given
        long userId = 1L;
        long chargePoint = 5000L;

        PointResponse charged = new PointResponse(userId, chargePoint, System.currentTimeMillis());
        PointChargeRequest request = new PointChargeRequest(chargePoint);
        given(pointChargeUseCase.ChargeUseCase(userId, chargePoint))
                .willReturn(new PointResponse(userId, chargePoint, System.currentTimeMillis()));

        // When & Then
        mockMvc.perform(patch("/point/charge/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(chargePoint))
                .andExpect(jsonPath("$.updateMillis").isNumber());

        // Verify
        verify(pointChargeUseCase).ChargeUseCase(userId, chargePoint);

    }



}
