package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create order")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public OrderResponse createOrder(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(jwt, request);
    }

    @Operation(summary = "Get orders")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrders(jwt);
    }

    @Operation(summary = "Cancel order")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }
}
