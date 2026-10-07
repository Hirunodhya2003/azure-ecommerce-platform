package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.order.CreateOrderRequest;
import com.ecommerce.ecommerce_backend.dto.order.OrderResponse;
import com.ecommerce.ecommerce_backend.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ecommerce.ecommerce_backend.model.OrderStatus;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request
    ) {

        User user = (User) authentication.getPrincipal();

        return orderService.createOrder(
                user.getId(),
                request
        );
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status
    ) {
        User user = (User) authentication.getPrincipal();

        return orderService.getMyOrders(
                user.getId(),
                status
        );
    }

    @GetMapping("/admin")
    public List<OrderResponse> getAllOrders(
            @RequestParam(required = false) OrderStatus status
    ) {

        return orderService.getAllOrders(status);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        User user = (User) authentication.getPrincipal();

        return orderService.getOrderById(
                user.getId(),
                orderId
        );
    }

    @PutMapping("/{orderId}/cancel")
    public OrderResponse cancelOrder(
            Authentication authentication,
            @PathVariable Long orderId
    ) {

        User user = (User) authentication.getPrincipal();

        return orderService.cancelOrder(
                user.getId(),
                orderId
        );
    }

    @PutMapping("/admin/{orderId}/status")
    public OrderResponse updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {

        return orderService.updateOrderStatus(
                orderId,
                request.getStatus()
        );
    }
}

