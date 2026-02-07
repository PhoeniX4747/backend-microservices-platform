package com.example.order.dto;

import com.example.order.model.OrderStatus;

import java.time.Instant;

public record OrderResponse(Long id, String userId, Long productId, int quantity, OrderStatus status, Instant createdAt) {
}
