package com.tus.payment;

public class PaypalStrategy extends PaymentStrategy {
	private final String emailId;
	private final String password;

	public PaypalStrategy(final String email, final String pwd) {
		this.emailId = email;
		this.password = pwd;
	}

	public String getEmailId() {
		return emailId;
	}

	public String getPassword() {
		return password;
	}

}
