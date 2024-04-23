package com.tus.exception;

public class MedAvailCustomerException extends MedAvailException {

	private static final long serialVersionUID = 334051992916748022L;

	public MedAvailCustomerException(final long customerAccountId) {
		super("Unknown Customer: "+ customerAccountId);
	}

}
