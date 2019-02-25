package com.sellio.pos.engine.db;

import com.sellio.pos.engine.Common;
import com.sellio.pos.engine.Utility;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Inventory extends RealmObject {
	@PrimaryKey private int id;

	private Product product;
	private double costPrice;
	private String mode;
	private Date dateAdded;
	private int initial;
	private int quantity;
	private int balance;
	private String name;
	private String status;
	private boolean synced;

	public Inventory() {
		synced = false;
	}

	public static Inventory create() {
		Inventory inventory = new Inventory();
		return inventory;
	}

	public static Inventory create(int id,
								   Product product,
								   double cost_price,
								   String mode,
								   Date dateAdded,
								   int initial,
								   int quantity,
								   int balance,
								   String name, String status) {
		Inventory inventory = Inventory.create();

		inventory.id = id;
		inventory.product = product;
		inventory.costPrice = cost_price;
		inventory.mode = mode;
		inventory.dateAdded = dateAdded;
		inventory.initial = initial;
		inventory.quantity = quantity;
		inventory.balance = balance;
		inventory.name = name;
		inventory.status = status;

		return inventory;
	}

	public static Inventory createForSale(Product product, int quantity) {
		Inventory inventory = Inventory.create();

		inventory.product = product;
		inventory.costPrice = product.getSalePrice();
		inventory.mode = Common.DB_INVENTORY_SALE;
		inventory.dateAdded = Calendar.getInstance().getTime();
		inventory.initial = 0;
		inventory.quantity = quantity;
		inventory.name = "";
		inventory.status = "";

		return inventory;
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

	public double getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(double costPrice) {
		this.costPrice = costPrice;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public int getInitial() {
		return initial;
	}

	public void setInitial(int initial) {
		this.initial = initial;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getBalance() {
		return balance;
	}

	public void setBalance(int balance) {
		this.balance = balance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public static class ApiInventory {
		public int id;

		public int productID;
		public double cost;
		public String mode;
		public Date date;
		public int initial;
		public int quantity;
		public int balance;
		public String name;
		public String status;

		public ApiInventory() {}

		public static ApiInventory from(Inventory inventory) {
			ApiInventory apiInventory = new ApiInventory();

			apiInventory.id = inventory.getId();
			apiInventory.productID = inventory.getProduct().getId();
			apiInventory.cost = inventory.getCostPrice();
			apiInventory.mode = inventory.getMode();
			apiInventory.date = inventory.getDateAdded();
			apiInventory.initial = inventory.getInitial();
			apiInventory.quantity = inventory.getQuantity();
			apiInventory.balance = inventory.getBalance();
			apiInventory.name = inventory.getName();
			apiInventory.status = inventory.getStatus();

			return apiInventory;
		}

		public static ApiInventory from(JSONObject info) {
			ApiInventory inventory = new ApiInventory();

			try {
				inventory.id = info.getInt("id");
				inventory.productID = info.getInt("product_id");
				inventory.cost = info.getDouble("cost");
				inventory.mode = info.getString("mode");
				inventory.date = Utility.convertTextToDate(info.getString("date"));
				inventory.initial = info.getInt("initial");
				inventory.quantity = info.getInt("quantity");
				inventory.name = info.getString("name");
				inventory.status = info.getString("status");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return inventory;
		}

		public Inventory inventory() {
			Inventory inventory = new Inventory();

			inventory.id = id;
			inventory.product = DatabaseManager.sharedInstance().productByID(productID);
			inventory.costPrice = cost;
			inventory.mode = mode;
			inventory.dateAdded = date;
			inventory.initial = initial;
			inventory.quantity = quantity;
			inventory.name = name;
			inventory.status = status;

			return inventory;
		}
	}
}
