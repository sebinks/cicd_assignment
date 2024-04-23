package com.tus.control;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.tus.dao.MedAvailDAO;
import com.tus.entities.Customer;
import com.tus.entities.Prescription;
import com.tus.entities.Product;
import com.tus.exception.MedAvailCustomerException;
import com.tus.exception.MedAvailOutOfStockException;
import com.tus.exception.MedAvailDAOException;
import com.tus.exception.MedAvailPrescriptionException;
import com.tus.exception.MedAvailException;
import com.tus.payment.CreditCardStrategy;
import com.tus.payment.PaypalStrategy;
import com.tus.services.CreditCardHandler;
import com.tus.services.Dispenser;
import com.tus.services.PayPalHandler;
import com.tus.services.PaymentHandlerFactory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyLong;
import org.mockito.internal.verification.Times;
//MG
class MedAvailControllerImplTest {
	private MedAvailControllerImpl medAvailControllerImpl;
	private Customer customer;
	private final Dispenser dispenser = mock(Dispenser.class);
	private final MedAvailDAO medAvailDAO = mock(MedAvailDAO.class);
	final static long CUSTOMER_ACCOUNT_ID = 123456L;
	final static long PRESCRIPTION_ID = 567890L;
	final static long PRODUCT_ONE_CODE = 11111L;
	final static long PRODUCT_TWO_CODE = 22222L;
	final static double PRODUCT_ONE_COST = 13.95; 
	final static double PRODUCT_TWO_COST = 11.15;
	final static String CUSTOMER_NAME = "Joe";
	final static String CUSTOMER_ADDRESS = "Athlone";
	final static String CUSTOMER_EMAIL = "joe@gmail.com";
	final static String CUSTOMER_CREDIT_CARD_NUM = "1234-5678";
	final static String CUSTOMER_CREDIT_CARD_CVV = "123";
	final static String CUSTOMER_CREDIT_CARD_EXPIRY = "12-21";
	final static String CUSTOMER_PAYPAL_PASSWORD = "abc";
	private PaypalStrategy paypalStrategy;
	private final PaymentHandlerFactory paymentHandlerFactory = mock(PaymentHandlerFactory.class);
	private CreditCardStrategy creditCardStrategy;
	private final PayPalHandler payPalHandler = mock(PayPalHandler.class);
	private final CreditCardHandler creditCardHandler = mock(CreditCardHandler.class);;
	private Product productOne;
	private Product productTwo;
	private Prescription prescription;

	@BeforeEach
	public void setUp() {
		paypalStrategy = new PaypalStrategy(CUSTOMER_EMAIL, CUSTOMER_PAYPAL_PASSWORD);
		
		creditCardStrategy = new CreditCardStrategy(CUSTOMER_NAME, CUSTOMER_CREDIT_CARD_NUM, CUSTOMER_CREDIT_CARD_CVV,
				CUSTOMER_CREDIT_CARD_EXPIRY);
		customer = new Customer(CUSTOMER_ACCOUNT_ID, CUSTOMER_NAME, CUSTOMER_ADDRESS, CUSTOMER_EMAIL, "CreditCard",
				creditCardStrategy);
		prescription = new Prescription();
		productOne = new Product(PRODUCT_ONE_CODE, PRODUCT_ONE_COST);
		productTwo = new Product(PRODUCT_TWO_CODE, PRODUCT_TWO_COST);
		prescription = new Prescription();
		prescription.addPrescriptionItem(productOne);
		medAvailControllerImpl = new MedAvailControllerImpl(medAvailDAO, dispenser, paymentHandlerFactory);

	}

//Test 1
	@Test
	void testCustomerNotFoundException() throws MedAvailException, SQLException {
		Throwable exception=assertThrows(MedAvailCustomerException.class, () -> {
			medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		});
		assertEquals("Unknown Customer: "+ CUSTOMER_ACCOUNT_ID, exception.getMessage());	
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(creditCardHandler, times(0)).pay(anyLong(), anyString(), anyString(), anyString(), anyString(),
				anyDouble());
		verify(dispenser, times(0)).dispense(anyLong());
	}

//Test 2
	@Test
	void testPrescriptionNotFoundException() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		Throwable exception=assertThrows(MedAvailPrescriptionException.class, () -> {
			medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		});
		assertEquals("Prescription not found: "+ PRESCRIPTION_ID, exception.getMessage());
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(creditCardHandler, times(0)).pay(anyLong(), anyString(), anyString(), anyString(), anyString(),
				anyDouble());
		verify(dispenser, times(0)).dispense(anyLong());
	}
	//Test 3
	@Test
	void testOneItemOnPrescriptionSuccess() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		when(medAvailDAO.getPrescriptionForId(PRESCRIPTION_ID)).thenReturn(prescription);
		when(medAvailDAO.checkProductInStock(PRODUCT_ONE_CODE)).thenReturn(true);
		when(paymentHandlerFactory.getPaymentHandler("CreditCard")).thenReturn(creditCardHandler);
		medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).checkProductInStock(PRODUCT_ONE_CODE);
		verify(creditCardHandler, times(1)).pay(PRESCRIPTION_ID, CUSTOMER_CREDIT_CARD_NUM, CUSTOMER_NAME,
				CUSTOMER_CREDIT_CARD_CVV, CUSTOMER_CREDIT_CARD_EXPIRY, PRODUCT_ONE_COST);
		verify(dispenser, times(1)).dispense(PRODUCT_ONE_CODE);
	}
//Test 4 
	@Test
	void testMedAvailDAOExceptionCustomer() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenThrow(SQLException.class);
		Throwable exception=assertThrows(MedAvailDAOException.class, () -> {
			medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		});
		assertEquals("Error in connection to MedAvail database", exception.getMessage());
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(0)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(creditCardHandler, times(0)).pay(anyLong(), anyString(), anyString(), anyString(), anyString(),
				anyDouble());
		verify(dispenser, times(0)).dispense(anyLong());
	}

	// Test5
	@Test
	void testMedAvailDAOExceptionPrescription() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		when(medAvailDAO.getPrescriptionForId(PRESCRIPTION_ID)).thenThrow(SQLException.class);
		Throwable exception=assertThrows(MedAvailDAOException.class, () -> {
			medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		});
		assertEquals("Error in connection to MedAvail database", exception.getMessage());
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(creditCardHandler, times(0)).pay(anyLong(), anyString(), anyString(), anyString(), anyString(),
				anyDouble());
		verify(dispenser, times(0)).dispense(anyLong());
	}


//Test 6	
	@Test
	void testOneItemOnPrescriptionCheckStockException() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		when(medAvailDAO.getPrescriptionForId(PRESCRIPTION_ID)).thenReturn(prescription);
		when(medAvailDAO.checkProductInStock(PRODUCT_ONE_CODE)).thenThrow(SQLException.class);
		Throwable exception=assertThrows(MedAvailDAOException.class, () -> {
			medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		});
		assertEquals("Error in connection to MedAvail database", exception.getMessage());
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).checkProductInStock(PRODUCT_ONE_CODE);
		verify(creditCardHandler, times(0)).pay(anyLong(), anyString(), anyString(), anyString(), anyString(),
				anyDouble());
		verify(dispenser, times(0)).dispense(anyLong());
	}

	// Test 7
	@Test
	void testOutOfStockDAOException() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		when(medAvailDAO.getPrescriptionForId(PRESCRIPTION_ID)).thenReturn(prescription);

		Throwable exception=assertThrows(MedAvailOutOfStockException.class, () -> {
			medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		});
		assertEquals("Product Out of Stock "+ PRODUCT_ONE_CODE, exception.getMessage());
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).checkProductInStock(PRODUCT_ONE_CODE);
		verify(creditCardHandler, times(0)).pay(anyLong(), anyString(), anyString(), anyString(), anyString(),
				anyDouble());
		verify(dispenser, times(0)).dispense(anyLong());
	}
	
//Test8
	@Test
	void testTwoItemsOnPrescriptionSuccess() throws MedAvailException, SQLException {
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		when(medAvailDAO.getPrescriptionForId(PRESCRIPTION_ID)).thenReturn(prescription);
		when(medAvailDAO.checkProductInStock(PRODUCT_ONE_CODE)).thenReturn(true);
		when(medAvailDAO.checkProductInStock(PRODUCT_TWO_CODE)).thenReturn(true);
		prescription.addPrescriptionItem(productTwo);
		when(paymentHandlerFactory.getPaymentHandler("CreditCard")).thenReturn(creditCardHandler);
		medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).checkProductInStock(PRODUCT_ONE_CODE);
		verify(medAvailDAO, new Times(1)).checkProductInStock(PRODUCT_TWO_CODE);
		verify(creditCardHandler, times(1)).pay(PRESCRIPTION_ID, CUSTOMER_CREDIT_CARD_NUM, CUSTOMER_NAME,
				CUSTOMER_CREDIT_CARD_CVV, CUSTOMER_CREDIT_CARD_EXPIRY, PRODUCT_ONE_COST + PRODUCT_TWO_COST);
		verify(dispenser, times(1)).dispense(PRODUCT_ONE_CODE);
		verify(dispenser, times(1)).dispense(PRODUCT_TWO_CODE);
	}

	// Test9
	@Test
	void testOneItemOnPrescriptionPayPal() throws MedAvailException, SQLException {
		customer.setPaymentType("PayPal");
		customer.setPaymentStrategy(paypalStrategy);
		when(medAvailDAO.getCustomerForId(CUSTOMER_ACCOUNT_ID)).thenReturn(customer);
		when(medAvailDAO.getPrescriptionForId(PRESCRIPTION_ID)).thenReturn(prescription);
		when(medAvailDAO.checkProductInStock(PRODUCT_ONE_CODE)).thenReturn(true);
		when(paymentHandlerFactory.getPaymentHandler("PayPal")).thenReturn(payPalHandler);
		medAvailControllerImpl.processPrescription(CUSTOMER_ACCOUNT_ID, PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).getCustomerForId(CUSTOMER_ACCOUNT_ID);
		verify(medAvailDAO, new Times(1)).getPrescriptionForId(PRESCRIPTION_ID);
		verify(medAvailDAO, new Times(1)).checkProductInStock(PRODUCT_ONE_CODE);
		verify(payPalHandler, times(1)).pay(PRESCRIPTION_ID, CUSTOMER_EMAIL, CUSTOMER_PAYPAL_PASSWORD, PRODUCT_ONE_COST);
		verify(dispenser, times(1)).dispense(PRODUCT_ONE_CODE);
	}

}

