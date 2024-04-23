package com.tus.entities;

import java.util.ArrayList;
import java.util.List;

public class Prescription {
	
	private final ArrayList<Product> prescriptionItems = new ArrayList<Product>();
	private long prescriptionId;

	public List<Product> getPrescriptionItems() {
		return prescriptionItems;
	}

	public void addPrescriptionItem(final Product product) {
		this.prescriptionItems.add(product);
	}

}
