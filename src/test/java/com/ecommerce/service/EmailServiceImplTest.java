package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import com.ecommerce.service.email.impl.EmailServiceImpl;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmailServiceImpl.
 *
 * Key fixes:
 * - supply a real MimeMessage via mailSender.createMimeMessage()
 * - inject a non-null 'from' value into EmailServiceImpl's private field (reflection),
 *   because @Value is not processed in this plain unit test.
 */
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create a real MimeMessage so MimeMessageHelper can operate on it
        mimeMessage = new MimeMessage((jakarta.mail.Session) null);

        // Ensure mailSender.createMimeMessage() returns our real MimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // IMPORTANT: Inject a non-null 'from' value into the EmailServiceImpl instance.
        // The service relies on @Value("${app.mail.from}") in runtime, but @Value is not applied here.
        Field fromField = EmailServiceImpl.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(emailService, "noreply@myshop.example"); // matches your config default
    }

    @Test
    void testSendOrderConfirmation_success() throws Exception {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Order order = new Order();
        order.setId(123L);
        order.setUser(user);

        // Mock template engine processing (avoid requiring real templates)
        when(templateEngine.process(eq("emails/order-confirmation"), any(Context.class)))
                .thenReturn("<html>Order Confirmation</html>");

        // Act
        emailService.sendOrderConfirmation(order); // direct call — @Async won't be active in this unit test

        // Assert: verify mailSender.send() is invoked once with a MimeMessage
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void testSendPaymentStatusUpdate_success() throws Exception {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Order order = new Order();
        order.setId(456L);
        order.setUser(user);

        // Act
        emailService.sendPaymentStatusUpdate(order, true);   // payment success
        emailService.sendPaymentStatusUpdate(order, false);  // payment failed

        // Assert: two sends should be attempted
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    void testSendOrderConfirmation_exceptionHandled() throws Exception {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");

        Order order = new Order();
        order.setId(789L);
        order.setUser(user);

        // Make template engine throw -> service should catch and not call send()
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template Error"));

        // Act
        emailService.sendOrderConfirmation(order);

        // Assert: mailSender.send() must never be called
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}