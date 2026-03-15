package com.ecommerce.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.OrderDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMode;
import com.ecommerce.enums.PaymentStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.email.EmailService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

	private final ModelMapper modelMapper;

	private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;
	private final ProductRepository productRepository;
	private final CartItemRepository cartItemRepository;
	private final EmailService emailService;

	public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
			ProductRepository productRepository, CartItemRepository cartItemRepository, ModelMapper modelMapper,
			EmailService emailService) {

		this.orderRepository = orderRepository;
		this.cartRepository = cartRepository;
		this.productRepository = productRepository;
		this.cartItemRepository = cartItemRepository;
		this.modelMapper = modelMapper;
		this.emailService = emailService;
	}

	/**
	 * Checkout cart and create order
	 */
	public OrderDto checkout(Long userId, PaymentMode paymentMode) {

		logger.info("User {} performing checkout", userId);

		Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Cart not found"));

		if (cart.getItems().isEmpty()) {
			throw new RuntimeException("Cart is empty");
		}

		Order order = new Order();
		order.setUser(cart.getUser());
		order.setOrderDate(Instant.now());
		order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
		order.setPaymentStatus(PaymentStatus.PENDING);
		order.setPaymentMode(paymentMode);

		List<OrderItem> orderItems = new ArrayList<>();

		for (CartItem item : cart.getItems()) {

			Product product = item.getProduct();

			if (product.getStock() < item.getQuantity()) {
				throw new RuntimeException("Product out of stock: " + product.getName());
			}

			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(order);
			orderItem.setProduct(product);
			orderItem.setQuantity(item.getQuantity());
			orderItem.setPrice(product.getPrice());

			orderItems.add(orderItem);
		}

		order.setItems(orderItems);

		double total = orderItems.stream().mapToDouble(i -> i.getPrice().doubleValue() * i.getQuantity()).sum();

		order.setTotalAmount(total);

		Order savedOrder = orderRepository.save(order);

		logger.info("Order {} created successfully", savedOrder.getId());

		cartItemRepository.deleteAll(cart.getItems());
		cart.getItems().clear();
		cartRepository.save(cart);
		// send confirmation asynchronously
		emailService.sendOrderConfirmation(order);

		return convertToDto(savedOrder);
	}

	/**
	 * Process payment simulation
	 */
	public OrderDto processPayment(Long orderId, boolean success) {

		logger.info("Processing payment for order {}", orderId);

		Order order = getOrderForCurrentUser(orderId);

		if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
			throw new RuntimeException("Order already paid");
		}

		if (success) {

			order.setPaymentStatus(PaymentStatus.SUCCESS);
			order.setOrderStatus(OrderStatus.PLACED);

			for (OrderItem item : order.getItems()) {

				Product product = item.getProduct();

				if (product.getStock() < item.getQuantity()) {
					throw new RuntimeException("Product out of stock");
				}

				product.setStock(product.getStock() - item.getQuantity());
				productRepository.save(product);
			}
			emailService.sendPaymentStatusUpdate(order, success);
			logger.info("Payment successful for order {}", orderId);

		} else {

			order.setPaymentStatus(PaymentStatus.FAILED);
			order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
			
			emailService.sendPaymentStatusUpdate(order, false);
			logger.warn("Payment failed for order {}", orderId);
		}
		

		return convertToDto(orderRepository.save(order));
	}

	/**
	 * Fetch orders for current user
	 */
	public List<OrderDto> getOrdersForCurrentUser() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

		boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		List<Order> orders;

		if (isAdmin) {
			logger.debug("Admin requesting all orders");
			orders = orderRepository.findAll();
		} else {
			logger.debug("Fetching orders for user {}", user.getUser().getId());
			orders = orderRepository.findByUserId(user.getUser().getId());
		}

		return orders.stream().map(this::convertToDto).collect(Collectors.toList());
	}

	/**
	 * ADMIN: Update order status
	 */
	public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {

		logger.warn("Updating order {} status to {}", orderId, status);

		OrderDto order = getOrder(orderId);

		order.setOrderStatus(status);

		return convertToDto(orderRepository.save(convertToEntity(order)));
	}

	/**
	 * Fetch order by ID
	 */
	public OrderDto getOrder(Long orderId) {
		return convertToDto(getOrderForCurrentUser(orderId));
	}

	/**
	 * Secure method ensuring user owns the order
	 */
	private Order getOrderForCurrentUser(Long orderId) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		boolean isAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		if (!isAdmin && !order.getUser().getId().equals(user.getUser().getId())) {
			throw new RuntimeException("Unauthorized access to this order");
		}

		return order;
	}

	/**
	 * Entity → DTO mapper
	 */
	private OrderDto convertToDto(Order order) {

		return modelMapper.map(order, OrderDto.class);
	}

	/**
	 * DTO → Entity mapper
	 */
	private Order convertToEntity(OrderDto order) {

		return modelMapper.map(order, Order.class);
	}
}