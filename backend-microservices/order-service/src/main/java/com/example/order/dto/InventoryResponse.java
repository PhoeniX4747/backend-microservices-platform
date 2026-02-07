package com.example.order.dto;

public record InventoryResponse(Long productId, String name, int availableQuantity) {
}
