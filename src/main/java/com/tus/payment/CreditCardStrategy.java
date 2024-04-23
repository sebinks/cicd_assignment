package com.tus.payment;

public class CreditCardStrategy extends PaymentStrategy {
	private final String cardName;
	private final String cardNumber;
	private final String cvv;
	private final String dateOfExpiry;

	public CreditCardStrategy(final String cardName, final String ccNum,
			final String cvv, final String expiryDate) {
		this.cardName = cardName;
		this.cardNumber = ccNum;
		this.cvv = cvv;
		this.dateOfExpiry = expiryDate;
	}

	public String getCardName() {
		return cardName;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public String getCvv() {
		return cvv;
	}

	public String getDateOfExpiry() {
		return dateOfExpiry;
	}

}
