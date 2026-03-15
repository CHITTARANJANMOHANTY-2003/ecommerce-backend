package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.ecommerce.entity.Order;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setup() {

        order = new Order();
        order.setId(1L);
        order.setPaymentStatus(PaymentStatus.PENDING);

    }

    /**
     * Test successful OR failed payment (random branch)
     */
    @Test
    void processPayment_updatesPaymentStatus() {

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = paymentService.processPayment(1L);

        assertNotNull(result);
        assertTrue(
                result.getPaymentStatus() == PaymentStatus.SUCCESS ||
                result.getPaymentStatus() == PaymentStatus.FAILED
        );

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    /**
     * Order not found scenario
     */
    @Test
    void processPayment_orderNotFound() {

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> paymentService.processPayment(1L)
        );

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }

}