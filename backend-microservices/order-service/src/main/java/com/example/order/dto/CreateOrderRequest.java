package com.example.order.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(@NotBlank String description) {
}
