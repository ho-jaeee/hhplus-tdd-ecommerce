package kr.hhplus.be.server.unittest.orderTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.controller.OrderController;
import kr.hhplus.be.server.order.controller.dto.OrderItemRequest;
import kr.hhplus.be.server.order.controller.dto.OrderRequest;

import kr.hhplus.be.server.order.usecase.OrderRedisUseCase;
import kr.hhplus.be.server.order.usecase.dto.OrderItemResult;
import kr.hhplus.be.server.order.usecase.dto.OrderResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



public class OrderControllerTest {

    @Test
    @DisplayName("POST /orders - 주문 요청 성공 시 201 Created")
    void createOrder_success() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        // given
        OrderRedisUseCase mockUseCase = Mockito.mock(OrderRedisUseCase.class);

        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(1001L, "티셔츠", 15000L, 2),
                new OrderItemRequest(1002L, "바지", 20000L, 1)
        );
        OrderRequest request = new OrderRequest(1L, 5L, items);


        OrderResult result = new OrderResult(
                10001L,
                1L,
                50000L,
                45000L,
                List.of(
                        new OrderItemResult(10L, "상품A", 2, 30000L),
                        new OrderItemResult(20L, "상품B", 1, 20000L)
                ),
                "PAID"
        );

        when(mockUseCase.createOrder(any())).thenReturn(result);

        OrderController controller = new OrderController(mockUseCase);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(10001L))
                .andExpect(jsonPath("$.discountedPrice").value(45000L))
                .andExpect(jsonPath("$.message").value("PAID"));


    }
}
