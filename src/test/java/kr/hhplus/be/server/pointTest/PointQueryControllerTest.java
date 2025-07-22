package kr.hhplus.be.server.pointTest;

import kr.hhplus.be.server.point.controller.PointQueryController;
import kr.hhplus.be.server.point.controller.dto.PointResponse;
import kr.hhplus.be.server.point.usecase.PointQueryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class PointQueryControllerTest {

    @Test
    @DisplayName("GET /point/{id} - 포인트 조회 성공")
    void GetPoint() throws Exception {

        //given
        long userId = 1L;
        PointResponse response = new PointResponse(userId, 10000L, System.currentTimeMillis());
        PointQueryUseCase mockUseCase = Mockito.mock(PointQueryUseCase.class);
        when(mockUseCase.QueryUseCase(userId)).thenReturn(response);


        PointQueryController controller = new PointQueryController(mockUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();


        // when & then
        mockMvc.perform(get("/point/{id}", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.point").value(10000L));

    }


}
