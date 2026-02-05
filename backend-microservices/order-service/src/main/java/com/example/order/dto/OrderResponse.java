package com.example.order.dto;

import com.example.order.model.OrderStatus;

import java.time.Instant;

public record OrderResponse(Long id, String userId, OrderStatus status, Instant createdAt) {
}
