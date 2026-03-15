package com.ecommerce.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

import com.ecommerce.enums.OrderStatus;
import com.ecommerce.enums.PaymentMode;
import com.ecommerce.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({"password", "email"})
	private User user;

	private Double totalAmount;

	private Instant orderDate;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private OrderStatus orderStatus;

	@Enumerated(EnumType.STRING)
	@Column(length = 30)
	private PaymentStatus paymentStatus;
	
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private PaymentMode paymentMode;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<OrderItem> items;

	// getters/setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Instant getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Instant orderDate) {
		this.orderDate = orderDate;
	}

	public com.ecommerce.enums.OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(com.ecommerce.enums.OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public com.ecommerce.enums.PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(com.ecommerce.enums.PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}
	
	public PaymentMode getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(PaymentMode paymentMode) {
		this.paymentMode = paymentMode;
	}

}