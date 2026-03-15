package com.ecommerce.dto;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentStatus;

import java.time.Instant;
import java.util.List;

public class OrderDto {

    private Long id;

    private Long userId;

    private Double totalAmount;

    private Instant orderDate;

    private PaymentStatus paymentStatus;

    private OrderStatus orderStatus;

    private List<OrderItemDto> items;

    public OrderDto() {}

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }
}