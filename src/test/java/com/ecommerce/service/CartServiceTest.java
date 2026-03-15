package com.ecommerce.service;

import com.ecommerce.dto.CartDto;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setup() {

        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Laptop");
        product.setPrice(BigDecimal.valueOf(1000));
        product.setStock(10);

        cart = new Cart();
        cart.setId(100L);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());
        cart.setTotalPrice(0.0);

        cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
    }

    /**
     * getCartByUser EXISTING CART
     */
    @Test
    void getCartByUser_existingCart() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getCartByUser(1L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    /**
     * getCartByUser CREATE NEW CART
     */
    @Test
    void getCartByUser_createNewCart() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.getCartByUser(1L);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    /**
     * addProductToCart SUCCESS
     */
    @Test
    void addProductToCart_success() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L))
                .thenReturn(Optional.empty());

        CartDto result = cartService.addProductToCart(1L, 10L, 2);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    /**
     * addProductToCart INVALID QUANTITY
     */
    @Test
    void addProductToCart_invalidQuantity() {

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addProductToCart(1L, 10L, 0));
    }

    /**
     * addProductToCart PRODUCT NOT FOUND
     */
    @Test
    void addProductToCart_productNotFound() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addProductToCart(1L, 10L, 1));
    }

    /**
     * updateCartItem SUCCESS
     */
    @Test
    void updateCartItem_success() {

        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L))
                .thenReturn(Optional.of(cartItem));

        cartService.updateCartItem(1L, 10L, 3);

        assertEquals(3, cartItem.getQuantity());
        verify(cartItemRepository).save(cartItem);
    }

    /**
     * updateCartItem ITEM NOT FOUND
     */
    @Test
    void updateCartItem_notFound() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateCartItem(1L, 10L, 2));
    }

    /**
     * removeProduct SUCCESS
     */
    @Test
    void removeProduct_success() {

        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(100L, 10L))
                .thenReturn(Optional.of(cartItem));

        CartDto result = cartService.removeProduct(1L, 10L);

        assertNotNull(result);
        verify(cartItemRepository).delete(cartItem);
    }

    /**
     * clearCart SUCCESS
     */
    @Test
    void clearCart_success() {

        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart(1L);

        verify(cartItemRepository).deleteAll(anyList());
        verify(cartRepository).save(cart);
    }

}