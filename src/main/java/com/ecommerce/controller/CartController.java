package com.ecommerce.controller;

import com.ecommerce.dto.CartDto;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.CartService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger =
            LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Add product to user's cart
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<CartDto> addProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam int quantity) {

        Long userId = userDetails.getUser().getId();

        logger.info("User {} adding product {} with quantity {} to cart",
                userId, productId, quantity);

        CartDto cart =
                cartService.addProductToCart(userId, productId, quantity);

        return ResponseEntity.ok(cart);
    }

    /**
     * Update quantity of a product in cart
     */
    @PutMapping("/update/{productId}")
    public ResponseEntity<CartDto> updateProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam int quantity) {

        Long userId = userDetails.getUser().getId();

        logger.info("User {} updating product {} quantity to {}",
                userId, productId, quantity);

        CartDto cart =
                cartService.updateCartItem(userId, productId, quantity);

        return ResponseEntity.ok(cart);
    }

    /**
     * Remove product from cart
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartDto> removeProduct(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {

        Long userId = userDetails.getUser().getId();

        logger.info("User {} removing product {} from cart",
                userId, productId);

        CartDto cart =
                cartService.removeProduct(userId, productId);

        return ResponseEntity.ok(cart);
    }

    /**
     * Fetch logged-in user's cart
     */
    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        logger.debug("Fetching cart for user {}", userId);

        CartDto cart =
                cartService.mapToDto(cartService.getCartByUser(userId));

        return ResponseEntity.ok(cart);
    }

    /**
     * Clear all items from cart
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        logger.warn("User {} clearing entire cart", userId);

        cartService.clearCart(userId);

        return ResponseEntity.ok().build();
    }
}