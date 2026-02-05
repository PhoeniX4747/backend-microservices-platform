package com.example.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReleaseStockRequest(@NotNull Long productId, @Min(1) int quantity) {
}
