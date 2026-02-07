package com.example.order.client;

import com.example.order.dto.InventoryResponse;
import com.example.order.dto.ReleaseStockRequest;
import com.example.order.dto.ReserveStockRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryFeignClient {

    @PostMapping("/api/v1/inventory/reserve")
    InventoryResponse reserve(@RequestBody ReserveStockRequest request);

    @PostMapping("/api/v1/inventory/release")
    InventoryResponse release(@RequestBody ReleaseStockRequest request);
}
