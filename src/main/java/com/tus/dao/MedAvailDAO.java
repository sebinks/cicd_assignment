package com.tus.dao;

import java.sql.SQLException;

import com.tus.entities.Customer;
import com.tus.entities.Prescription;

public interface MedAvailDAO  {
	Customer getCustomerForId(long customerAccountId) throws SQLException;
	Prescription getPrescriptionForId(long prescriptionId) throws SQLException;
	boolean checkProductInStock(long productId) throws SQLException;
	
}
