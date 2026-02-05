package com.example.inventory.controller;

import com.example.inventory.dto.*;
import com.example.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "Reserve stock")
    @PostMapping("/reserve")
    public InventoryResponse reserve(@Valid @RequestBody ReserveStockRequest request) {
        return inventoryService.reserve(request);
    }

    @Operation(summary = "Release stock")
    @PostMapping("/release")
    public InventoryResponse release(@Valid @RequestBody ReleaseStockRequest request) {
        return inventoryService.release(request);
    }

    @Operation(summary = "Update stock")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/update")
    public InventoryResponse update(@Valid @RequestBody UpdateStockRequest request) {
        return inventoryService.update(request);
    }
}
