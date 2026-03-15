package com.ecommerce.controller;

import com.ecommerce.config.JwtAuthenticationEntryPoint;
import com.ecommerce.dto.OrderDto;
import com.ecommerce.dto.OrderItemDto;
import com.ecommerce.entity.User;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMode;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.enums.Role;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationFilter;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.OrderService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    /* ---------- SECURITY MOCKS ---------- */

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

    /* ------------------------------------ */
   

    /* ---------- Helper Methods ---------- */

    private CustomUserDetails buildUserPrincipal() {

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("user@gmail.com");
        user.setPassword("encoded");
        user.setRole(Role.ROLE_CUSTOMER);

        return new CustomUserDetails(user);
    }

    private OrderDto buildOrderDto() {

        OrderItemDto item = new OrderItemDto();
        item.setProductId(10L);
        item.setProductName("iPhone 15");
        item.setQuantity(2);
        item.setPrice(75000.0);

        OrderDto order = new OrderDto();
        order.setId(100L);
        order.setUserId(1L);
        order.setTotalAmount(150000.0);
        order.setOrderDate(Instant.now());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        order.setItems(List.of(item));

        return order;
    }

    private void setAuthentication(CustomUserDetails user) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /* ------------------------------------ */

    @Test
    void testCheckout_success() throws Exception {

        CustomUserDetails user = buildUserPrincipal();
        setAuthentication(user);

        OrderDto order = buildOrderDto();

        when(orderService.checkout(1L, PaymentMode.COD))
                .thenReturn(order);

        mockMvc.perform(post("/api/orders/checkout")
                .param("paymentMode", "COD")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.totalAmount").value(150000.0));

        verify(orderService).checkout(1L, PaymentMode.COD);

        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetOrders_success() throws Exception {

        OrderDto order = buildOrderDto();

        when(orderService.getOrdersForCurrentUser())
                .thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        verify(orderService).getOrdersForCurrentUser();
    }

    @Test
    void testGetOrder_success() throws Exception {

        OrderDto order = buildOrderDto();

        when(orderService.getOrder(100L)).thenReturn(order);

        mockMvc.perform(get("/api/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        verify(orderService).getOrder(100L);
    }

    @Test
    void testSimulatePayment_success() throws Exception {

        OrderDto order = buildOrderDto();
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setOrderStatus(OrderStatus.PLACED);

        when(orderService.processPayment(100L, true))
                .thenReturn(order);

        mockMvc.perform(post("/api/orders/100/pay")
                .param("success", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));

        verify(orderService).processPayment(100L, true);
    }

    @Test
    void testUpdateStatus_success() throws Exception {

        OrderDto order = buildOrderDto();
        order.setOrderStatus(OrderStatus.SHIPPED);

        when(orderService.updateOrderStatus(100L, OrderStatus.SHIPPED))
                .thenReturn(order);

        mockMvc.perform(put("/api/orders/100/status")
                .param("status", "SHIPPED")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("SHIPPED"));

        verify(orderService).updateOrderStatus(100L, OrderStatus.SHIPPED);
    }
}