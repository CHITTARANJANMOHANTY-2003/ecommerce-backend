package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.entity.*;
import com.ecommerce.enums.*;
import com.ecommerce.repository.*;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.email.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ModelMapper modelMapper;

    // ✅ NEW MOCK
    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private Order order;
    private OrderDto orderDto;

    @BeforeEach
    void setup() {

        user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_ADMIN);

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(BigDecimal.valueOf(50000));
        product.setStock(10);

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderDate(Instant.now());

        orderDto = new OrderDto();
        orderDto.setId(1L);
    }

    private void mockSecurity(User user) {

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void checkout_success() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        OrderDto result = orderService.checkout(1L, PaymentMode.COD);

        assertNotNull(result);

        verify(orderRepository).save(any(Order.class));
        verify(cartItemRepository).deleteAll(cart.getItems());

        // ✅ verify email was sent
        verify(emailService).sendOrderConfirmation(any(Order.class));
    }

    @Test
    void checkout_cartNotFound() {

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderService.checkout(1L, PaymentMode.COD));
    }

    @Test
    void getOrdersForCurrentUser_admin() {

        mockSecurity(user);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        List<OrderDto> result = orderService.getOrdersForCurrentUser();

        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getOrder_success() {

        mockSecurity(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        OrderDto result = orderService.getOrder(1L);

        assertNotNull(result);
    }

    @Test
    void getOrder_notFound() {

        mockSecurity(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderService.getOrder(1L));
    }

    @Test
    void processPayment_success() {

        mockSecurity(user);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPrice(product.getPrice());

        order.setItems(List.of(orderItem));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        OrderDto result = orderService.processPayment(1L, true);

        assertNotNull(result);

        verify(productRepository).save(product);

        // ✅ verify email sent
        verify(emailService).sendPaymentStatusUpdate(order, true);
    }

    @Test
    void processPayment_failed() {

        mockSecurity(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        OrderDto result = orderService.processPayment(1L, false);

        assertNotNull(result);

        // ✅ verify email sent
        verify(emailService).sendPaymentStatusUpdate(order, false);
    }

}