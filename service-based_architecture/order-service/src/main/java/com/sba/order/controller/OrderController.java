package com.sba.order.controller;

import com.sba.common.dto.OrderDto;
import com.sba.common.dto.UpdateOrderStatusRequest;
import com.sba.order.dto.CreateOrderRequest;
import com.sba.order.service.OrderService;
import com.sba.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final JwtService jwtService;

    @PostMapping("/orders")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                                @RequestHeader("Authorization") String bearerToken) {
        return ResponseEntity.ok(orderService.createOrder(request, bearerToken));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7);
        Long userId = jwtService.extractUserId(token);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }
}
