package com.tus.exception;

public class MedAvailOutOfStockException extends MedAvailException {

	private static final long serialVersionUID = 334051992916748022L;

	public MedAvailOutOfStockException(final long productId) {
		super("Product Out of Stock "+ productId);
	}

}
