package com.tus.entities;

import com.tus.payment.PaymentStrategy;

public class Customer {

	private long customerAccountId;
	private String customerName;
	private String customerEmail;
	private String customerAddress;
	private String paymentType;
	private PaymentStrategy paymentStrategy;
	
	public Customer(final long customerAccountId, final String customerName, final String customerAddress,final String customerEmail,
			final String paymentType, final PaymentStrategy paymentStrategy) {
		this.customerAccountId = customerAccountId;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.customerAddress = customerAddress;
		this.paymentType = paymentType;
		this.paymentStrategy = paymentStrategy;
	}

	

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(final String paymentType) {
		this.paymentType = paymentType;
	}

	public PaymentStrategy getPaymentStrategy() {
		return paymentStrategy;
	}

	public void setPaymentStrategy(final PaymentStrategy paymentStrategy) {
		this.paymentStrategy = paymentStrategy;
	}


}
