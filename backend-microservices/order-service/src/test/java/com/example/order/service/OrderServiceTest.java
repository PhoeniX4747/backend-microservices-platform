package com.example.order.service;

import com.example.order.model.OrderEntity;
import com.example.order.model.OrderStatus;
import com.example.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import com.example.order.client.InventoryFeignClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryFeignClient inventoryFeignClient;
    @InjectMocks private OrderService orderService;

    @Test
    void userShouldSeeOnlyOwnOrders() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), Map.of("sub", userId.toString(), "role", "USER"));
        OrderEntity entity = new OrderEntity();
        entity.setUserId(userId);
        entity.setStatus(OrderStatus.CREATED);

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(entity));

        assertEquals(1, orderService.getOrders(jwt).size());
    }
}
