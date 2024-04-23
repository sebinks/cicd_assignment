package com.tus.exception;

public class MedAvailDAOException extends MedAvailException {

	private static final long serialVersionUID = 334051992916748022L;

	public MedAvailDAOException() {
		super("Error in connection to MedAvail database");
	}

}
