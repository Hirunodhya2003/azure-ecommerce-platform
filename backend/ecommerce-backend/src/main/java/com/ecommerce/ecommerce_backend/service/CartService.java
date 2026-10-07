package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.cart.AddToCartRequest;
import com.ecommerce.ecommerce_backend.dto.cart.CartItemResponse;
import com.ecommerce.ecommerce_backend.dto.cart.CartResponse;
import com.ecommerce.ecommerce_backend.dto.cart.UpdateCartItemRequest;
import com.ecommerce.ecommerce_backend.exception.CartItemNotFoundException;
import com.ecommerce.ecommerce_backend.exception.CartNotFoundException;
import com.ecommerce.ecommerce_backend.exception.ProductNotFoundException;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.repository.CartItemRepository;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    // GET CURRENT USER CART
    @Transactional
    public CartResponse getCart(Long userId) {

        User user = getUser(userId);

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        return mapToCartResponse(cart);
    }


    // ADD PRODUCT TO CART
    @Transactional
    public CartResponse addToCart(
            Long userId,
            AddToCartRequest request
    ) {

        User user = getUser(userId);

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> createCart(user));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(
                        () -> new ProductNotFoundException(
                                "Product not found with id: "
                                        + request.getProductId()
                        )
                );

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException(
                    "Product is not active"
            );
        }

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException(
                    "Insufficient stock for product: "
                            + product.getName()
            );
        }

        CartItem cartItem =
                cartItemRepository
                        .findByCartAndProduct(cart, product)
                        .orElse(null);

        if (cartItem != null) {

            int newQuantity =
                    cartItem.getQuantity()
                            + request.getQuantity();

            if (newQuantity > product.getStock()) {
                throw new IllegalArgumentException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem(
                    cart,
                    product,
                    request.getQuantity(),
                    product.getPrice()
            );
        }

        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }


    // UPDATE CART ITEM QUANTITY
    @Transactional
    public CartResponse updateCartItem(
            Long userId,
            Long productId,
            UpdateCartItemRequest request
    ) {

        User user = getUser(userId);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(
                        () -> new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(
                                () -> new CartItemNotFoundException(
                                        "Product is not in the cart"
                                )
                        );

        Product product = cartItem.getProduct();

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException(
                    "Product is not active"
            );
        }

        if (request.getQuantity() > product.getStock()) {
            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock"
            );
        }

        cartItem.setQuantity(
                request.getQuantity()
        );

        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }


    // REMOVE PRODUCT FROM CART
    @Transactional
    public CartResponse removeFromCart(
            Long userId,
            Long productId
    ) {

        User user = getUser(userId);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(
                        () -> new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(
                                () -> new CartItemNotFoundException(
                                        "Product is not in the cart"
                                )
                        );

        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        cartRepository.save(cart);

        return mapToCartResponse(cart);
    }


    // CLEAR CART
    @Transactional
    public void clearCart(Long userId) {

        User user = getUser(userId);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(
                        () -> new CartNotFoundException(
                                "Cart not found for user: " + userId
                        )
                );

        cartItemRepository.deleteByCartId(
                cart.getId()
        );

        cart.setUpdatedAt(
                java.time.LocalDateTime.now()
        );

        cartRepository.save(cart);
    }


    // GET USER
    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User not found with id: " + userId
                        )
                );
    }


    // CREATE CART
    private Cart createCart(User user) {

        Cart cart = new Cart(user);

        return cartRepository.save(cart);
    }


    // ENTITY → RESPONSE
    private CartResponse mapToCartResponse(
            Cart cart
    ) {

        List<CartItem> cartItems =
                cartItemRepository.findByCart(cart);

        List<CartItemResponse> itemResponses =
                cartItems.stream()
                        .map(this::mapToCartItemResponse)
                        .toList();

        BigDecimal totalAmount =
                itemResponses.stream()
                        .map(CartItemResponse::getSubtotal)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getActive(),
                itemResponses,
                totalAmount
        );
    }


    // CART ITEM → RESPONSE
    private CartItemResponse mapToCartItemResponse(
            CartItem cartItem
    ) {

        BigDecimal subtotal =
                cartItem.getUnitPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        cartItem.getQuantity()
                                )
                        );

        Product product = cartItem.getProduct();

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                cartItem.getQuantity(),
                cartItem.getUnitPrice(),
                subtotal
        );
    }
}