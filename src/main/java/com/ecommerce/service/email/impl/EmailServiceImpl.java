package com.ecommerce.service.email.impl;

import com.ecommerce.entity.Order;
import com.ecommerce.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.slf4j.Logger;

@Service
public class EmailServiceImpl implements EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
		super();
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	@Value("${app.mail.from:noreply@myshop.example}")
    private String from;

    @Async("emailExecutor")
    @Override
    public void sendOrderConfirmation(Order order) {
        try {
            Context ctx = new Context();
            ctx.setVariable("order", order);
            String html = templateEngine.process("emails/order-confirmation", ctx);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");
            helper.setText(html, true);
            helper.setTo(order.getUser().getEmail());
            helper.setSubject("Order Confirmation - #" + order.getId());
            helper.setFrom(from);
            mailSender.send(msg);
            log.info("Order confirmation email queued for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation for order {}: {}", order.getId(), e.getMessage(), e);
            // optionally persist failure for retries
        }
    }

    @Async("emailExecutor")
    @Override
    public void sendPaymentStatusUpdate(Order order, boolean success) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
            helper.setTo(order.getUser().getEmail());
            helper.setSubject(success ? "Payment Successful - Order #" + order.getId() : "Payment Failed - Order #" + order.getId());
            String body = success ? "Your payment has been received. We will process your order." :
                                   "Your payment failed. Please try again or contact support.";
            helper.setText(body, false);
            helper.setFrom(from);
            mailSender.send(msg);
            log.info("Payment status email queued for order {} success={}", order.getId(), success);
        } catch (Exception e) {
            log.error("Failed to send payment status update for order {}: {}", order.getId(), e.getMessage(), e);
        }
    }
}