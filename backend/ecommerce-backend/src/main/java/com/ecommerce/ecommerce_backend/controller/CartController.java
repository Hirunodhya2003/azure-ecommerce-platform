package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.cart.AddToCartRequest;
import com.ecommerce.ecommerce_backend.dto.cart.CartResponse;
import com.ecommerce.ecommerce_backend.dto.cart.UpdateCartItemRequest;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET CURRENT USER CART
    @GetMapping
    public CartResponse getCart(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        return cartService.getCart(user.getId());
    }


    // ADD PRODUCT TO CART
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        User user = (User) authentication.getPrincipal();

        return cartService.addToCart(
                user.getId(),
                request
        );
    }


    // UPDATE PRODUCT QUANTITY
    @PutMapping("/items/{productId}")
    public CartResponse updateCartItem(
            Authentication authentication,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        User user = (User) authentication.getPrincipal();

        return cartService.updateCartItem(
                user.getId(),
                productId,
                request
        );
    }


    // REMOVE PRODUCT FROM CART
    @DeleteMapping("/items/{productId}")
    public CartResponse removeFromCart(
            Authentication authentication,
            @PathVariable Long productId) {

        User user = (User) authentication.getPrincipal();

        return cartService.removeFromCart(
                user.getId(),
                productId
        );
    }


    // CLEAR CART
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        cartService.clearCart(user.getId());
    }
}