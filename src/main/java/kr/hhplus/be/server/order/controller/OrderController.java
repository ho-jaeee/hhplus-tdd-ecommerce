package kr.hhplus.be.server.order.controller;


import kr.hhplus.be.server.order.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.model.OrderJPA;
import kr.hhplus.be.server.order.usecase.OrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderUseCase orderUseCase;

    @PostMapping
    public ResponseEntity<OrderJPA> createOrder(@RequestBody OrderRequest request) {
        OrderJPA order = orderUseCase.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
