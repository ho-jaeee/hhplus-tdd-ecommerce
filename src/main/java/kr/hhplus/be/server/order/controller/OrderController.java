package kr.hhplus.be.server.order.controller;


import kr.hhplus.be.server.order.controller.dto.OrderRequest;
import kr.hhplus.be.server.order.controller.dto.OrderResponse;
import kr.hhplus.be.server.order.usecase.OrderUseCase;
import kr.hhplus.be.server.order.usecase.OrderCommand;
import kr.hhplus.be.server.order.usecase.OrderResult;
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
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        OrderCommand command = OrderRequest.toCommand(request); // 변환은 여기서!
        OrderResult result = orderUseCase.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(result));
    }
}
