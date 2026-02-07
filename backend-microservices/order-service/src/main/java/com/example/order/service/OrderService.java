package com.example.order.service;

import com.example.order.dto.OrderResponse;
import com.example.order.exception.ApiException;
import com.example.order.model.OrderEntity;
import com.example.order.model.OrderStatus;
import com.example.order.repository.OrderRepository;
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

    @Transactional
    public OrderResponse createOrder(Jwt jwt) {
        OrderEntity order = new OrderEntity();
        order.setUserId(UUID.fromString(jwt.getSubject()));
        order.setStatus(OrderStatus.CREATED);
        OrderEntity saved = orderRepository.save(order);
        return map(saved);
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
        order.setStatus(OrderStatus.CANCELLED);
        return map(orderRepository.save(order));
    }

    private OrderResponse map(OrderEntity entity) {
        return new OrderResponse(entity.getId(), entity.getUserId().toString(), entity.getStatus(), entity.getCreatedAt());
    }
}
