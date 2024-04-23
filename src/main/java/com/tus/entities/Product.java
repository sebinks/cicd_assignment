package com.tus.entities;

public class Product {
	public Product(final long productCode, final double productCost) {
		this.productCode = productCode;
		this.productCost = productCost;
	}


	private final long productCode;
	private final double productCost;

	public long getProductCode() {
		return productCode;
	}

	public double getProductCost() {
		return productCost;
	}

}
