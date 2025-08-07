package kr.hhplus.be.server.integrationTest.orderTest;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.controller.dto.OrderItemRequest;
import kr.hhplus.be.server.order.controller.dto.OrderRequest;
import kr.hhplus.be.server.point.domain.model.UserPointJPA;
import kr.hhplus.be.server.point.domain.repository.UserPointRepository;
import kr.hhplus.be.server.product.domain.model.ProductJPA;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrderControllerTestIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserPointRepository userPointRepository;


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long productId1;
    private Long productId2;


    @BeforeEach
    void setUp() {

        // 사용자 포인트 설정
        UserPointJPA user = new UserPointJPA(1L, 1000000L, System.currentTimeMillis());
        userPointRepository.save(user);

        // 실제 상품 2개 등록
        ProductJPA product1 = productRepository.save(new ProductJPA(
                null, "티셔츠", 15000L, 10, null, LocalDateTime.now(), LocalDateTime.now()
        ));
        ProductJPA product2 = productRepository.save(new ProductJPA(
                null, "바지", 20000L, 10, null, LocalDateTime.now(), LocalDateTime.now()
        ));
        productId1 = product1.getProductId();
        productId2 = product2.getProductId();
    }

    @Test
    @DisplayName("POST /orders - 실제 주문 요청 성공 (201 Created)")
    void createOrder_success() throws Exception {
        // given
        List<OrderItemRequest> items = List.of(
                new OrderItemRequest(productId1, "티셔츠", 15000L, 2),
                new OrderItemRequest(productId2, "바지", 20000L, 1)
        );
        OrderRequest request = new OrderRequest(1L, null, items);

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.totalPrice").value(50000L))
                .andExpect(jsonPath("$.message").value("PAID")); // 상태 메시지
    }
}
