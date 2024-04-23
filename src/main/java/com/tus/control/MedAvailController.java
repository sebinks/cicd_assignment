package com.tus.control;

import com.tus.exception.MedAvailException;

public interface MedAvailController {
	void processPrescription(long customerAccountId, long prescriptionId) throws MedAvailException;
}
