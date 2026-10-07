package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.order.CreateOrderRequest;
import com.ecommerce.ecommerce_backend.dto.order.OrderItemResponse;
import com.ecommerce.ecommerce_backend.dto.order.OrderResponse;
import com.ecommerce.ecommerce_backend.exception.CartNotFoundException;
import com.ecommerce.ecommerce_backend.exception.OrderNotFoundException;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.OrderItem;
import com.ecommerce.ecommerce_backend.model.OrderStatus;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.repository.CartItemRepository;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import com.ecommerce.ecommerce_backend.repository.OrderItemRepository;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(
            Long userId,
            CreateOrderRequest request
    ) {

        User user = getUser(userId);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(
                        () -> new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create order from an empty cart"
            );
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new IllegalArgumentException(
                        "Product is not active: "
                                + product.getName()
                );
            }

            if (cartItem.getQuantity() > product.getStock()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            BigDecimal subtotal =
                    cartItem.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem.getQuantity()
                                    )
                            );

            totalAmount = totalAmount.add(subtotal);
        }

        Order order = new Order(
                user,
                totalAmount,
                request.getShippingAddress()
        );

        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            BigDecimal subtotal =
                    cartItem.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem.getQuantity()
                                    )
                            );

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice(),
                    subtotal
            );

            orderItemRepository.save(orderItem);

            product.setStock(
                    product.getStock()
                            - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        cartItemRepository.deleteByCartId(
                cart.getId()
        );

        cart.setUpdatedAt(LocalDateTime.now());

        cartRepository.save(cart);

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long userId,
            Long orderId
    ) {

        Order order =
                orderRepository
                        .findByIdAndUserId(orderId, userId)
                        .orElseThrow(
                                () -> new OrderNotFoundException(
                                        "Order not found with id: "
                                                + orderId
                                )
                        );

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(
            Long userId,
            OrderStatus status
    ) {

        User user = getUser(userId);

        List<Order> orders;

        if (status == null) {

            orders = orderRepository
                    .findByUserOrderByCreatedAtDesc(user);

        } else {

            orders = orderRepository
                    .findByUserAndStatusOrderByCreatedAtDesc(
                            user,
                            status
                    );
        }

        return orders
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(
            OrderStatus status
    ) {

        List<Order> orders;

        if (status == null) {

            orders = orderRepository
                    .findAllByOrderByCreatedAtDesc();

        } else {

            orders = orderRepository
                    .findByStatusOrderByCreatedAtDesc(status);
        }

        return orders
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(
            Long userId,
            Long orderId
    ) {

        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(
                        () -> new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        OrderStatus currentStatus = order.getStatus();

        if (currentStatus == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException(
                    "Delivered orders cannot be cancelled"
            );
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Order is already cancelled"
            );
        }

        if (currentStatus == OrderStatus.SHIPPED) {
            throw new IllegalArgumentException(
                    "Shipped orders cannot be cancelled"
            );
        }

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        for (OrderItem orderItem : orderItems) {

            Product product = orderItem.getProduct();

            product.setStock(
                    product.getStock()
                            + orderItem.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder =
                orderRepository.save(order);

        return mapToOrderResponse(cancelledOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            OrderStatus newStatus
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        OrderStatus currentStatus = order.getStatus();

        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid order status transition: "
                            + currentStatus
                            + " -> "
                            + newStatus
            );
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        return mapToOrderResponse(updatedOrder);
    }

    private boolean isValidStatusTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {

        if (currentStatus == newStatus) {
            return true;
        }

        return switch (currentStatus) {

            case PENDING ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.PROCESSING
                            || newStatus == OrderStatus.CANCELLED;

            case PROCESSING ->
                    newStatus == OrderStatus.SHIPPED
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPED ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED,
                 CANCELLED ->
                    false;
        };
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );
    }

    private OrderResponse mapToOrderResponse(
            Order order
    ) {

        List<OrderItem> orderItems =
                orderItemRepository.findByOrder(order);

        List<OrderItemResponse> itemResponses =
                orderItems.stream()
                        .map(this::mapToOrderItemResponse)
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    private OrderItemResponse mapToOrderItemResponse(
            OrderItem orderItem
    ) {

        Product product = orderItem.getProduct();

        return new OrderItemResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getSubtotal()
        );
    }
}

