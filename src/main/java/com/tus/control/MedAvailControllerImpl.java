package com.tus.control;

import java.sql.SQLException;
import java.util.List;

import com.tus.entities.Customer;
import com.tus.entities.Prescription;
import com.tus.entities.Product;
import com.tus.dao.MedAvailDAO;
import com.tus.exception.MedAvailCustomerException;
import com.tus.exception.MedAvailDAOException;
import com.tus.exception.MedAvailException;
import com.tus.exception.MedAvailPrescriptionException;
import com.tus.exception.MedAvailOutOfStockException;
import com.tus.payment.CreditCardStrategy;
import com.tus.payment.PaymentStrategy;
import com.tus.payment.PaypalStrategy;
import com.tus.services.CreditCardHandler;
import com.tus.services.Dispenser;
import com.tus.services.PayPalHandler;
import com.tus.services.PaymentHandlerFactory;

public class MedAvailControllerImpl implements MedAvailController{
	private final MedAvailDAO medAvailDAO;
	private Customer customer;
	private long prescriptionId;
	private Prescription prescription;
	private final Dispenser dispenser;
	private final PaymentHandlerFactory paymentHandlerFactory;
	private List<Product> prescriptionItems;

	public MedAvailControllerImpl(final MedAvailDAO medAvailDAO,
			final Dispenser dispenser,
			final PaymentHandlerFactory paymentHandlerFactory) {
		this.medAvailDAO = medAvailDAO;
		this.dispenser = dispenser;
		this.paymentHandlerFactory = paymentHandlerFactory;
	}

	public void processPrescription(final long customerAccountId, final long prescriptionId) throws MedAvailException{
		try {
			this.prescriptionId = prescriptionId;
			customer = medAvailDAO.getCustomerForId(customerAccountId);
			prescription = medAvailDAO.getPrescriptionForId(prescriptionId);
			if (customer == null) {
				throw new MedAvailCustomerException(customerAccountId);
			} else if(prescription==null) {
				throw new MedAvailPrescriptionException(prescriptionId);
			} else {
				prescriptionItems=prescription.getPrescriptionItems();
				handlePrescription(prescriptionItems);

			}
		} catch (SQLException e) {
			throw new MedAvailDAOException();
		}
	}

	private void handlePrescription(final List<Product> prescriptionItems) throws MedAvailException {
		// prescriptionDetails = prescription.getPrescriptionDetails();
		double prescriptionTotalCost = 0;
		for (final Product product : prescriptionItems) {
			try {
				final long productCode = product.getProductCode();
				if (medAvailDAO.checkProductInStock(productCode)) {
					prescriptionTotalCost += product.getProductCost();
				} else {
					throw new MedAvailOutOfStockException(productCode);
				}
			} catch (SQLException e) {
				throw new MedAvailDAOException();
			}

		}
		processPayment(prescriptionTotalCost);
		dispenseMedicine(prescriptionItems);

	}

	private void processPayment(final double prescriptionTotalCost) throws MedAvailException {
		final PaymentStrategy paymentStrategy = customer.getPaymentStrategy();
			if (customer.getPaymentType().equals("CreditCard")) {
				final CreditCardStrategy creditCardStrategy = ((CreditCardStrategy) paymentStrategy);
				final CreditCardHandler creditCardHandler = (CreditCardHandler) paymentHandlerFactory
						.getPaymentHandler("CreditCard");
				creditCardHandler.pay(prescriptionId, creditCardStrategy.getCardNumber(),
						creditCardStrategy.getCardName(), creditCardStrategy.getCvv(),
						creditCardStrategy.getDateOfExpiry(), prescriptionTotalCost);
			} else {
				final PaypalStrategy payPalStrategy = ((PaypalStrategy) paymentStrategy);
				final PayPalHandler payPalHandler = (PayPalHandler) paymentHandlerFactory.getPaymentHandler("PayPal");
				payPalHandler.pay(prescriptionId, payPalStrategy.getEmailId(), payPalStrategy.getPassword(),
						prescriptionTotalCost);
			}
	}

	private void dispenseMedicine(final List<Product> prescriptionItems) {
		for (final Product product : prescriptionItems) {
			dispenser.dispense(product.getProductCode());
			}	
	}

	
}
