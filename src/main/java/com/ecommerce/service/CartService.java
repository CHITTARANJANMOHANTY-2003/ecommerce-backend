package com.ecommerce.service;

import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger logger =
            LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository, ModelMapper modelMapper) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Fetch user's cart or create a new one if it doesn't exist
     */
    public Cart getCartByUser(Long userId) {

        logger.debug("Fetching cart for user ID {}", userId);

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    logger.info("Cart not found for user {}. Creating new cart.", userId);

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> {
                                logger.error("User not found with ID {}", userId);
                                return new ResourceNotFoundException("User not found");
                            });

                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setTotalPrice(0.0);
                    cart.setItems(new ArrayList<>());

                    Cart savedCart = cartRepository.save(cart);

                    logger.info("New cart created with ID {}", savedCart.getId());

                    return savedCart;
                });
    }

    /**
     * Add product to cart
     */
    @Transactional
    public CartDto addProductToCart(Long userId, Long productId, int quantity) {

        logger.info("Adding product {} to cart for user {}", productId, userId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getCartByUser(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existingItem =
                cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        CartItem cartItem;

        if (existingItem.isPresent()) {

            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Not enough stock available");
            }

            cartItem.setQuantity(newQuantity);

        } else {

            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("Not enough stock available");
            }

            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        updateCartTotal(cart);

        return mapToDto(cart);
    }

    /**
     * Update cart item quantity
     */
    @Transactional
    public CartDto updateCartItem(Long userId, Long productId, int quantity) {

        logger.info("Updating cart item {} for user {}", productId, userId);

        Cart cart = getCartByUser(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Item not in cart"));

        if (item.getProduct().getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        item.setQuantity(quantity);

        cartItemRepository.save(item);

        updateCartTotal(cart);

        return mapToDto(cart);
    }

    /**
     * Remove product from cart
     */
    @Transactional
    public CartDto removeProduct(Long userId, Long productId) {

        logger.info("Removing product {} from cart for user {}", productId, userId);

        Cart cart = getCartByUser(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Item not found"));

        cart.getItems().remove(item);

        cartItemRepository.delete(item);

        updateCartTotal(cart);

        return mapToDto(cart);
    }

    /**
     * Clear entire cart
     */
    @Transactional
    public void clearCart(Long userId) {

        logger.warn("Clearing cart for user {}", userId);

        Cart cart = getCartByUser(userId);

        cartItemRepository.deleteAll(cart.getItems());

        cart.getItems().clear();

        cart.setTotalPrice(0.0);

        cartRepository.save(cart);
    }

    /**
     * Update cart total price
     */
    private void updateCartTotal(Cart cart) {

        double total = cart.getItems()
                .stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();

        cart.setTotalPrice(total);

        cartRepository.save(cart);

        logger.debug("Cart {} total updated to {}", cart.getId(), total);
    }

    /**
     * Convert Cart → CartDto
     */
    public CartDto mapToDto(Cart cart) {

        CartDto dto = new CartDto();

        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setTotalPrice(cart.getTotalPrice());

        List<CartItemDto> items = cart.getItems().stream().map(item -> {

            CartItemDto itemDto = new CartItemDto();

            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setQuantity(item.getQuantity());

            double price = item.getProduct().getPrice().doubleValue();

            itemDto.setPrice(price);
            itemDto.setSubtotal(price * item.getQuantity());

            return itemDto;

        }).toList();

        dto.setItems(items);

        return dto;
    }
}