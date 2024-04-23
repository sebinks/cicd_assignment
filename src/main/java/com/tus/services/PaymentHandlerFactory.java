package com.tus.services;

public interface PaymentHandlerFactory {
	 PaymentHandler getPaymentHandler(String type);
}
