package com.ecommerce.controller;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMode;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Checkout cart and create a new order
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam PaymentMode paymentMode) {

        Long userId = userDetails.getUser().getId();

        logger.info("User {} initiating checkout with payment mode {}",
                userId, paymentMode);

        OrderDto order = orderService.checkout(userId, paymentMode);

        logger.info("Order {} created successfully for user {}",
                order.getId(), userId);

        return ResponseEntity.ok(order);
    }

    /**
     * Fetch order history for logged-in user
     * Admin users will receive all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderDto>> getOrders() {

        logger.debug("Fetching orders for current user");

        List<OrderDto> orders = orderService.getOrdersForCurrentUser();

        return ResponseEntity.ok(orders);
    }

    /**
     * Fetch specific order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long id) {

        logger.debug("Fetching order with ID {}", id);

        OrderDto order = orderService.getOrder(id);

        return ResponseEntity.ok(order);
    }

    /**
     * Simulate payment processing
     * success=true → payment success
     * success=false → payment failure
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderDto> simulatePayment(
            @PathVariable Long id,
            @RequestParam boolean success) {

        logger.info("Processing payment for order {} (success={})", id, success);

        OrderDto order = orderService.processPayment(id, success);

        logger.info("Payment processed for order {}", id);

        return ResponseEntity.ok(order);
    }

    /**
     * ADMIN: Update order status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        logger.warn("Admin updating order {} status to {}", id, status);

        OrderDto order = orderService.updateOrderStatus(id, status);

        return ResponseEntity.ok(order);
    }
}