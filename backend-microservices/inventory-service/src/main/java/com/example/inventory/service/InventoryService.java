package com.example.inventory.service;

import com.example.inventory.dto.InventoryResponse;
import com.example.inventory.dto.ReleaseStockRequest;
import com.example.inventory.dto.ReserveStockRequest;
import com.example.inventory.dto.UpdateStockRequest;
import com.example.inventory.exception.ApiException;
import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;

    @Transactional
    public InventoryResponse reserve(ReserveStockRequest request) {
        Product product = findProduct(request.productId());
        if (product.getAvailableQuantity() < request.quantity()) {
            throw new ApiException("Not enough stock");
        }
        product.setAvailableQuantity(product.getAvailableQuantity() - request.quantity());
        return map(productRepository.save(product));
    }

    @Transactional
    public InventoryResponse release(ReleaseStockRequest request) {
        Product product = findProduct(request.productId());
        product.setAvailableQuantity(product.getAvailableQuantity() + request.quantity());
        return map(productRepository.save(product));
    }

    @Transactional
    public InventoryResponse update(UpdateStockRequest request) {
        Product product = productRepository.findById(request.productId()).orElse(new Product());
        product.setName(request.name());
        product.setAvailableQuantity(request.quantity());
        return map(productRepository.save(product));
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ApiException("Product not found"));
    }

    private InventoryResponse map(Product product) {
        return new InventoryResponse(product.getId(), product.getName(), product.getAvailableQuantity());
    }
}
