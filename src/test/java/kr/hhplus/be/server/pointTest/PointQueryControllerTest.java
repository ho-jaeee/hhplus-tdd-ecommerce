package kr.hhplus.be.server.pointTest;

import kr.hhplus.be.server.point.controller.PointQueryController;
import kr.hhplus.be.server.point.domain.model.UserPoint;
import kr.hhplus.be.server.point.usecase.PointQueryUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class PointQueryControllerTest {


    @Mock
    PointQueryUseCase pointQueryUseCase;
    MockMvc mockMvc;

    @InjectMocks
    PointQueryController pointQueryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pointQueryController).build();
    }

    @Test
    void GetPointQueryTest() throws Exception {

        //given
        long userId = 1L;
        long point = 10000L;
        long updateMillis = System.currentTimeMillis();

        given(pointQueryUseCase.QueryUseCase(userId))
                .willReturn(new UserPoint(userId, point, updateMillis));

        //when & then
        mockMvc.perform(get("/point/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(point))
                .andExpect(jsonPath("$.updateMillis").isNumber());

    }



}
