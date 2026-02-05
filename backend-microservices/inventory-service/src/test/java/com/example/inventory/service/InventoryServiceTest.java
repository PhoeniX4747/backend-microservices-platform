package com.example.inventory.service;

import com.example.inventory.dto.ReserveStockRequest;
import com.example.inventory.exception.ApiException;
import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private ProductRepository productRepository;
    @InjectMocks private InventoryService inventoryService;

    @Test
    void shouldPreventOverselling() {
        Product product = new Product();
        product.setName("p1");
        product.setAvailableQuantity(2);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(ApiException.class, () -> inventoryService.reserve(new ReserveStockRequest(1L, 5)));
    }
}
