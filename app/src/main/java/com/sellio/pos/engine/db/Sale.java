package com.sellio.pos.engine.db;

import com.sellio.pos.engine.Utility;

import org.json.JSONObject;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Sale extends RealmObject {
	@PrimaryKey  private int id;
	private RealmList<Cart> carts;
	private Date date;
	private String status;
	private boolean synced;

	public Sale() {
		synced = false;
	}

	public static Sale create() {
		return new Sale();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public RealmList<Cart> getCarts() {
		return carts;
	}

	public void setCarts(RealmList<Cart> carts) {
		this.carts = carts;
	}

	public double getTotalPrice() {
		double totalPrice = 0;
		for (Cart cart : carts) {
			totalPrice += cart.getTotalPrice();
		}
		return totalPrice;
	}

	public double getProfit() {
		double profit = 0;
		for (Cart cart : carts) {
			profit += (cart.getProduct().getSalePrice() - cart.getProduct().getCostPrice()) * cart.getQuantity();
		}
		return profit;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public String getCartInfo() {
		String cartInfo = "";
		for (Cart cart : carts) {
			cartInfo += cart.getId() + ",";
		}
		return cartInfo;
	}

	public static RealmList<Cart> parseCart(String cartInfo) {
		String[] cartIDs = cartInfo.split(",");

		RealmList<Cart> result = new RealmList<>();
		for (String cartID : cartIDs) {
			if (cartID.equals(""))
				continue;

			Cart cart = DatabaseManager.sharedInstance().cartByID(Integer.parseInt(cartID));
			if (cart.isValid())
				result.add(cart);
		}
		return result;
	}

	public static class ApiSale {
		public int id;
		public String cartInfo;
		public Date date;
		public String status;

		public ApiSale() {}

		public static ApiSale from(Sale sale) {
			ApiSale apiSale = new ApiSale();

			apiSale.id = sale.getId();
			apiSale.cartInfo = sale.getCartInfo();
			apiSale.date = sale.getDate();
			apiSale.status = sale.getStatus();

			return apiSale;
		}

		public static ApiSale create(JSONObject info) {
			ApiSale sale = new ApiSale();

			try {
				sale.id = info.getInt("id");
				sale.cartInfo = info.getString("cartInfo");
				sale.date = Utility.convertTextToDate(info.getString("date"));
				sale.status = info.getString("status");
			} catch (Exception e) {
				e.printStackTrace();
			}

			return sale;
		}

		public Sale sale() {
			Sale sale = Sale.create();

			sale.setId(id);
			sale.setCarts(parseCart(cartInfo));
			sale.setDate(date);
			sale.setStatus(status);

			return sale;
		}
	}
}
