package com.sellio.pos.engine.db;

import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Cart extends RealmObject {

	@PrimaryKey private int id;
	private Product product;
	private int quantity;
	private double unitPrice;
	private double totalPrice;
	private String status;
	private boolean synced;

	public Cart() {
		synced = false;
	}

	public static Cart create() {
		return new Cart();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public static class ApiCart {
		public int id;
		public int productID;
		public int quantity;
		public double unitPrice;
		public double totalPrice;
		public String status;

		public ApiCart() {}

		public static ApiCart from(Cart cart) {
			ApiCart apiCart = new ApiCart();

			apiCart.id = cart.getId();
			apiCart.productID = cart.getProduct().getId();
			apiCart.quantity = cart.getQuantity();
			apiCart.unitPrice = cart.getUnitPrice();
			apiCart.totalPrice = cart.getTotalPrice();
			apiCart.status = cart.getStatus();

			return apiCart;
		}

		public static ApiCart from(JSONObject info) {
			ApiCart cart = new ApiCart();

			try {
				cart.id = info.getInt("id");
				cart.productID = info.getInt("product_id");
				cart.quantity = info.getInt("quantity");
				cart.unitPrice = info.getDouble("unitPrice");
				cart.totalPrice = info.getInt("totalPrice");
				cart.status = info.getString("status");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return cart;
		}

		public Cart cart() {
			Cart cart = Cart.create();

			cart.id = id;
			cart.product = DatabaseManager.sharedInstance().productByID(productID);
			cart.quantity = quantity;
			cart.unitPrice = unitPrice;
			cart.totalPrice = totalPrice;
			cart.status = status;

			return cart;
		}
	}
}
