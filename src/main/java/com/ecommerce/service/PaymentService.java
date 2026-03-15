package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Simulate payment processing
     */
    public Order processPayment(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Random random = new Random();

        boolean paymentSuccess = random.nextBoolean();

        if (paymentSuccess) {

            order.setPaymentStatus(PaymentStatus.SUCCESS);

            logger.info("Payment successful for order {}", orderId);

        } else {

            order.setPaymentStatus(PaymentStatus.FAILED);

            logger.warn("Payment failed for order {}", orderId);
        }

        return orderRepository.save(order);
    }
}