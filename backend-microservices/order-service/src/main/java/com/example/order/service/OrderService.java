package com.example.order.service;

import com.example.order.client.InventoryFeignClient;
import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.InventoryResponse;
import com.example.order.dto.OrderResponse;
import com.example.order.dto.ReleaseStockRequest;
import com.example.order.dto.ReserveStockRequest;
import com.example.order.exception.ApiException;
import com.example.order.model.OrderEntity;
import com.example.order.model.OrderStatus;
import com.example.order.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryFeignClient inventoryFeignClient;

    @Transactional
    public OrderResponse createOrder(Jwt jwt, CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setUserId(UUID.fromString(jwt.getSubject()));
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setStatus(OrderStatus.CREATED);
        OrderEntity savedOrder = orderRepository.save(order);

        try {
            ReserveStockRequest reserveRequest = new ReserveStockRequest(savedOrder.getProductId(), savedOrder.getQuantity());
            InventoryResponse inventoryResponse = inventoryFeignClient.reserve(reserveRequest);
            validateInventoryResponse(inventoryResponse, savedOrder.getProductId());

            savedOrder.setStatus(OrderStatus.CONFIRMED);
            return map(orderRepository.save(savedOrder));
        } catch (FeignException | ApiException ex) {
            savedOrder.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);
            throw new ApiException("Order cancelled: unable to reserve inventory");
        }
    }

    public List<OrderResponse> getOrders(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if ("ADMIN".equals(role)) {
            return orderRepository.findAll().stream().map(this::map).toList();
        }
        UUID userId = UUID.fromString(jwt.getSubject());
        return orderRepository.findByUserId(userId).stream().map(this::map).toList();
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        OrderEntity order = orderRepository.findById(id).orElseThrow(() -> new ApiException("Order not found"));

        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new ApiException("Order already cancelled");
        }

        if (OrderStatus.CONFIRMED.equals(order.getStatus())) {
            try {
                ReleaseStockRequest releaseRequest = new ReleaseStockRequest(order.getProductId(), order.getQuantity());
                InventoryResponse inventoryResponse = inventoryFeignClient.release(releaseRequest);
                validateInventoryResponse(inventoryResponse, order.getProductId());
            } catch (FeignException | ApiException ex) {
                throw new ApiException("Unable to release reserved inventory for cancelled order");
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        return map(orderRepository.save(order));
    }


    private void validateInventoryResponse(InventoryResponse inventoryResponse, Long expectedProductId) {
        if (inventoryResponse == null || inventoryResponse.productId() == null) {
            throw new ApiException("Inventory response is invalid");
        }
        if (!expectedProductId.equals(inventoryResponse.productId())) {
            throw new ApiException("Inventory response product mismatch");
        }
    }

    private OrderResponse map(OrderEntity entity) {
        return new OrderResponse(
                entity.getId(),
                entity.getUserId().toString(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
