package com.ecommerce.controller;

import com.ecommerce.config.JwtAuthenticationEntryPoint;
import com.ecommerce.dto.CartDto;
import com.ecommerce.dto.CartItemDto;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationFilter;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.CartService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    /* -------- SECURITY MOCKS -------- */

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthenticationManager authenticationManager;

    /* -------------------------------- */


    /* ---------- Test Helpers ---------- */

    private CustomUserDetails buildUserPrincipal() {

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@gmail.com");
        user.setPassword("encoded");
        user.setRole(Role.ROLE_CUSTOMER);

        return new CustomUserDetails(user);
    }

    private CartDto buildCartDto() {

        CartItemDto item = new CartItemDto();
        item.setProductId(10L);
        item.setProductName("iPhone 15");
        item.setQuantity(2);
        item.setPrice(75000.0);
        item.setSubtotal(150000.0);

        CartDto cart = new CartDto();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setTotalPrice(150000.0);
        cart.setItems(List.of(item));

        return cart;
    }

    private void setAuthentication(CustomUserDetails user) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /* ---------------------------------- */

    @Test
    void testAddProduct_success() throws Exception {

        CustomUserDetails user = buildUserPrincipal();
        setAuthentication(user);

        CartDto cart = buildCartDto();

        when(cartService.addProductToCart(1L, 10L, 2)).thenReturn(cart);

        mockMvc.perform(post("/api/cart/add/10")
                .param("quantity", "2")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(150000.0))
                .andExpect(jsonPath("$.items[0].productName").value("iPhone 15"));

        verify(cartService).addProductToCart(1L, 10L, 2);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testUpdateProduct_success() throws Exception {

        CustomUserDetails user = buildUserPrincipal();
        setAuthentication(user);

        CartDto cart = buildCartDto();

        when(cartService.updateCartItem(1L, 10L, 3)).thenReturn(cart);

        mockMvc.perform(put("/api/cart/update/10")
                .param("quantity", "3")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").exists());

        verify(cartService).updateCartItem(1L, 10L, 3);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testRemoveProduct_success() throws Exception {

        CustomUserDetails user = buildUserPrincipal();
        setAuthentication(user);

        CartDto cart = buildCartDto();

        when(cartService.removeProduct(1L, 10L)).thenReturn(cart);

        mockMvc.perform(delete("/api/cart/remove/10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").exists());

        verify(cartService).removeProduct(1L, 10L);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCart_success() throws Exception {

        CustomUserDetails user = buildUserPrincipal();
        setAuthentication(user);

        CartDto cart = buildCartDto();

        when(cartService.getCartByUser(1L)).thenReturn(null);
        when(cartService.mapToDto(any())).thenReturn(cart);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.items[0].productName").value("iPhone 15"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void testClearCart_success() throws Exception {

        CustomUserDetails user = buildUserPrincipal();
        setAuthentication(user);

        doNothing().when(cartService).clearCart(1L);

        mockMvc.perform(delete("/api/cart/clear")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService).clearCart(1L);

        SecurityContextHolder.clearContext();
    }
}