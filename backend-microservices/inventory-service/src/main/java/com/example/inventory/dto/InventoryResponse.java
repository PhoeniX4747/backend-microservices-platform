package com.example.inventory.dto;

public record InventoryResponse(Long productId, String name, int availableQuantity) {
}
