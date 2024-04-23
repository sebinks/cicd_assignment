package com.tus.services;



public interface PayPalHandler extends PaymentHandler {
	void pay(long prescriptionId, String email, String password, double amount);
}
