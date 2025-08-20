package kr.hhplus.be.server.integrationTest.productTest;

import kr.hhplus.be.server.product.domain.service.dto.ProductPopularDto;
import kr.hhplus.be.server.product.usecase.ProductPopularRankingAllTimeUseCase;
import kr.hhplus.be.server.product.usecase.ProductPopularRankingDayUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc
public class ProductPopularControllersTestIntegration {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProductPopularRankingDayUseCase dayUseCase;

    @MockBean
    ProductPopularRankingAllTimeUseCase allTimeUseCase;

    // ---------- 오늘 Top-N ----------
    @Test
    @DisplayName("GET /products/rank/today - 기본 n=10")
    void today_defaultN() throws Exception {
        var resp = List.of(
                new ProductPopularDto(1001L, "상품A", 12L),
                new ProductPopularDto(2002L, "상품B", 7L)
        );
        when(dayUseCase.getTodayTopN(eq(10))).thenReturn(resp);

        mockMvc.perform(get("/products/rank/today").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId", is(1001)))
                .andExpect(jsonPath("$[0].productName", is("상품A")))
                .andExpect(jsonPath("$[0].score", is(12)))
                .andExpect(jsonPath("$[1].productId", is(2002)))
                .andExpect(jsonPath("$[1].productName", is("상품B")))
                .andExpect(jsonPath("$[1].score", is(7)));

        verify(dayUseCase).getTodayTopN(10);
    }

    @Test
    @DisplayName("GET /products/rank/today?n=5 - n 전달 검증")
    void today_customN() throws Exception {
        when(dayUseCase.getTodayTopN(eq(5))).thenReturn(List.of());

        mockMvc.perform(get("/products/rank/today")
                        .param("n", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(dayUseCase).getTodayTopN(5);
    }

    // ---------- 최근 7일 Top-N ----------
    @Test
    @DisplayName("GET /products/rank/weekly - 기본 n=10")
    void weekly_defaultN() throws Exception {
        var resp = List.of(new ProductPopularDto(3003L, "상품C", 20L));
        when(dayUseCase.getLast7DaysTopN(eq(10))).thenReturn(resp);

        mockMvc.perform(get("/products/rank/weekly").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId", is(3003)))
                .andExpect(jsonPath("$[0].productName", is("상품C")))
                .andExpect(jsonPath("$[0].score", is(20)));

        verify(dayUseCase).getLast7DaysTopN(10);
    }

    @Test
    @DisplayName("GET /products/rank/weekly?n=3 - n 전달 검증")
    void weekly_customN() throws Exception {
        when(dayUseCase.getLast7DaysTopN(eq(3))).thenReturn(List.of());

        mockMvc.perform(get("/products/rank/weekly")
                        .param("n", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(dayUseCase).getLast7DaysTopN(3);
    }

    // ---------- 전체(올타임) Top-N ----------
    @Test
    @DisplayName("GET /products/rank/all-time - 기본값(n 미전달)")
    void allTime_default() throws Exception {
        var resp = List.of(new ProductPopularDto(4004L, "상품D", 99L));
        when(allTimeUseCase.execute(isNull())).thenReturn(resp);

        mockMvc.perform(get("/products/rank/all-time").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId", is(4004)))
                .andExpect(jsonPath("$[0].productName", is("상품D")))
                .andExpect(jsonPath("$[0].score", is(99)));

        verify(allTimeUseCase).execute(null);
    }

    @Test
    @DisplayName("GET /products/rank/all-time?n=7 - n 전달 검증")
    void allTime_customN() throws Exception {
        when(allTimeUseCase.execute(eq(7))).thenReturn(List.of());

        mockMvc.perform(get("/products/rank/all-time")
                        .param("n", "7")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(allTimeUseCase).execute(7);
    }
}
