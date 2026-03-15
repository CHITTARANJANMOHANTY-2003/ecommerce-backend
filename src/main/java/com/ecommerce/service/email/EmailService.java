package com.ecommerce.service.email;

import com.ecommerce.entity.Order;

public interface EmailService {
    void sendOrderConfirmation(Order order);
    void sendPaymentStatusUpdate(Order order, boolean success);
}