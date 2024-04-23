package com.tus.exception;

public class MedAvailPrescriptionException extends MedAvailException {

	private static final long serialVersionUID = 334051992916748022L;

	public MedAvailPrescriptionException(final long prescriptionId) {
		super("Prescription not found: "+ prescriptionId);
	}

}
