package com.sellio.pos.engine.db;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sellio.pos.SellioApplication;
import com.sellio.pos.engine.Common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

public class DatabaseManager extends Object {
	private static DatabaseManager instance = null;
	public static DatabaseManager sharedInstance() {
		if (instance == null)
			instance = new DatabaseManager();
		return instance;
	}

	private Callback callback = null;
	public interface Callback {
		void OnSuccess();
		void OnFailure(String message);
	}

	public void runOnSuccess() {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null)
					callback.OnSuccess();
			}
		});
	}

	public void runOnFailure(String message) {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null)
					callback.OnFailure(message);
			}
		});
	}

	private Realm realm;

	private DatabaseManager() {
		Realm.init(SellioApplication.sharedInstance());
		RealmConfiguration config = new RealmConfiguration.Builder().name("sellio_db.realm").deleteRealmIfMigrationNeeded().build();
		Realm.setDefaultConfiguration(config);
		realm = Realm.getDefaultInstance();
	}

	public ArrayList<String> categories() {
		ArrayList<Product> products = new ArrayList<>(realm.where(Product.class)
				.equalTo("deleted", false).distinct("category").findAll());
		ArrayList<String> categories = new ArrayList<>();
		for (Product product : products) {
			categories.add(product.getCategory());
		}

		return categories;
	}

	private int getAvailableProductId() {
		Number currentIdNum = realm.where(Product.class).max("id");
		int index = (currentIdNum == null ? 0 : currentIdNum.intValue()+1);
		return index;
	}

	private int getAvailableInventoryId() {
		Number currentIdNum = realm.where(Inventory.class).max("id");
		int index = (currentIdNum == null ? 0 : currentIdNum.intValue()+1);
		return index;
	}

	private int getAvailableCartId() {
		Number currentIdNum = realm.where(Cart.class).max("id");
		int index = (currentIdNum == null ? 0 : currentIdNum.intValue()+1);
		return index;
	}

	private int getAvailableSaleId() {
		Number currentIdNum = realm.where(Sale.class).max("id");
		int index = (currentIdNum == null ? 0 : currentIdNum.intValue()+1);
		return index;
	}

	public ArrayList<Product> productsByCategory(String category) {
		if (category == null || category.equals("") || category.equals("ALL")) {
			return allProducts();
		}

		ArrayList<Product> result = new ArrayList<>(realm.where(Product.class)
				.equalTo("category", category).and()
				.equalTo("deleted", false).findAll());
		return result;
	}

	public ArrayList<Product> productsByKey(String key) {
		ArrayList<Product> result = new ArrayList<>(realm.where(Product.class)
				.contains("name", key).and()
				.equalTo("deleted", false).findAll());
		return result;
	}

	public Product productByID(int id) {
		Product product = realm.where(Product.class)
				.equalTo("id", id).findFirst();
		return product;
	}

	public ArrayList<Product> allProducts() {
		return new ArrayList<>(realm.where(Product.class)
				.equalTo("deleted", false)
				.sort("id", Sort.DESCENDING).findAll());
	}

	public void addProduct(Product product, Callback callback) {
		this.callback = callback;

		RealmResults<Product> result = realm.where(Product.class)
				.equalTo("name", product.getName()).and()
				.equalTo("deleted", false).findAll();
		if (result.size() > 0) {
			runOnFailure("Found the product with same name. Please use another name.");
			return;
		}

		product.setId(getAvailableProductId());

		try {
			realm.beginTransaction();
			product = realm.copyToRealm(product);
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public void addProduct(Product product) {
		try {
			realm.beginTransaction();
			product = realm.copyToRealm(product);
			realm.commitTransaction();
		} catch (Exception e) {
			realm.cancelTransaction();
		}
	}

	public void updateProduct(Product product, Callback callback) {
		this.callback = callback;

		ArrayList<Product> products = new ArrayList<>(realm.where(Product.class).equalTo("id", product.getId()).findAll());
		if (products.size() <= 0) {
			runOnFailure("No such product added.");
			return;
		}

		Product lastOne = products.get(0);
		realm.executeTransaction(new Realm.Transaction() {
			@Override
			public void execute(Realm realm) {
				try {
					lastOne.setName(product.getName());
					lastOne.setBarcode(product.getBarcode());
					lastOne.setUnit(product.getUnit());
					lastOne.setCostPrice(product.getCostPrice());
					lastOne.setSalePrice(product.getSalePrice());
					lastOne.setCategory(product.getCategory());
					lastOne.setPhoto(product.getPhoto());
					lastOne.setSynced(false);

					runOnSuccess();
				} catch (Exception e) {
					runOnFailure(e.getMessage());
				}
			}
		});
	}

	public void saleProduct(Product product, int quantity) {
		try {
			Inventory inventory = Inventory.createForSale(product, quantity);
			inventory.setId(getAvailableInventoryId());

			realm.beginTransaction();
			ArrayList<Inventory> inventories = new ArrayList<Inventory>(realm.where(Inventory.class).equalTo("product.id", product.getId()).sort("id").findAll());
			if (inventories.size() <= 0) {
				inventory.setBalance(-quantity);
				inventory.setSynced(false);
				realm.copyToRealm(inventory);
			} else {
				Inventory lastOne = inventories.get(inventories.size()-1);
				inventory.setBalance(lastOne.getBalance() - quantity);
				inventory.setSynced(false);
				realm.copyToRealm(inventory);
			}
			realm.commitTransaction();
		} catch (Exception e) {
			Log.e("product", e.getMessage());
		}
	}

	public void deleteProduct(Product product, Callback callback) {
		this.callback = callback;

		try {
			realm.beginTransaction();
			product.setDeleted(true);
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public ArrayList<Inventory> inventoriesByKey(String key) {
		if (key == null || key.equals("")) {
			return new ArrayList<>(realm.where(Inventory.class).findAll());
		}

		RealmResults<Inventory> results = realm.where(Inventory.class)
				.contains("name", key).or()
				.contains("mode", key).or()
				.contains("product.name", key).or()
				.contains("product.unit", key).or()
				.contains("product.category", key)
				.sort("id", Sort.DESCENDING).findAll();


		return new ArrayList<>(results);
	}

	public ArrayList<Inventory> inventoriesByProduct(Product product, Calendar startTime, Calendar endTime) {
		if (startTime == null || endTime == null) {
			return new ArrayList<>();
		}

		Date startDate = startTime.getTime();
		Date endDate = endTime.getTime();

		RealmResults<Inventory> results = realm.where(Inventory.class)
				.equalTo("product.id", product.getId())
				.sort("id", Sort.DESCENDING).findAll();
		ArrayList<Inventory> inventoriesByProduct = new ArrayList<>(results);
		ArrayList<Inventory> result = new ArrayList<>();
		for (Inventory inventory : inventoriesByProduct) {
			Date date = inventory.getDateAdded();
			if (date.after(startDate) && date.before(endDate)) {
				result.add(inventory);
			} else if (inventory.getDateAdded().equals(startTime) || inventory.getDateAdded().equals(endTime)) {
				result.add(inventory);
			}
		}

		return result;
	}

	public ArrayList<Inventory> inventoriesForBalance(String key) {
		ArrayList<Product> products = new ArrayList<>(realm.where(Product.class).findAll());

		ArrayList<Inventory> result = new ArrayList<>();
		for (Product product : products) {
			ArrayList<Inventory> inventories = new ArrayList<>(realm.where(Inventory.class)
					.equalTo("product.id", product.getId()).findAll());
			if (inventories.size() <= 0)
				continue;

			Inventory inventory = inventories.get(inventories.size()-1);
			if (key == null || key.equals("")) {
				result.add(inventory);
				continue;
			}

			if (inventory.getProduct().getName().contains(key)
					|| inventory.getProduct().getCategory().contains(key)
					|| inventory.getProduct().getUnit().contains(key)) {
				result.add(inventory);
			}
		}

		return result;
	}

	public void purchaseProduct(Product product, double price, int quantity, Callback callback) {
		this.callback = callback;

		try {
			Inventory inventory = Inventory.create(
					getAvailableInventoryId(),
					product,
					price,
					Common.DB_INVENTORY_PURCHASE,
					Calendar.getInstance().getTime(),
					0,
					quantity,
					0,
					"",
					""
			);

			RealmResults<Inventory> results = realm.where(Inventory.class)
					.equalTo("product.id", inventory.getProduct().getId())
					.sort("id", Sort.DESCENDING).findAll();
			ArrayList<Inventory> inventoriesByProduct = new ArrayList<>(results);
			if (inventoriesByProduct.size() <= 0) {
				inventory.setBalance(inventory.getQuantity());
				inventory.setSynced(false);
			} else {
				Inventory lastOne = inventoriesByProduct.get(inventoriesByProduct.size()-1);
				inventory.setBalance(inventory.getQuantity() + lastOne.getBalance());
				inventory.setSynced(false);
			}

			realm.beginTransaction();
			realm.copyToRealm(inventory);
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public void addInventory(Inventory inventory) {
		try {
			realm.beginTransaction();
			inventory = realm.copyToRealm(inventory);
			realm.commitTransaction();
		} catch (Exception e) {
			realm.cancelTransaction();
			e.printStackTrace();
		}
	}

	public ArrayList<Cart> carts() {
		return new ArrayList<>(realm.where(Cart.class)
				.sort("id", Sort.DESCENDING)
				.equalTo("status", Common.DB_CART_ONSALE).findAll());
	}

	public Cart cartByID(int id) {
		return realm.where(Cart.class).equalTo("id", id).findFirst();
	}

	public void addCart(Product product, int quantity, Callback callback) {
		this.callback = callback;

		try {
			ArrayList<Cart> carts = new ArrayList<>(realm.where(Cart.class)
					.equalTo("product.id", product.getId())
					.equalTo("status", Common.DB_CART_ONSALE).findAll());
			if (carts.size() > 0) {
				Cart cart = carts.get(0);

				realm.beginTransaction();
				cart.setQuantity(cart.getQuantity() + quantity);
				cart.setTotalPrice(cart.getQuantity() * cart.getUnitPrice());
				realm.commitTransaction();

				runOnSuccess();
				return;
			}

			Cart cart = Cart.create();
			cart.setId(getAvailableCartId());
			cart.setProduct(product);
			cart.setQuantity(quantity);
			cart.setUnitPrice(product.getSalePrice());
			cart.setTotalPrice(product.getSalePrice() * quantity);
			cart.setStatus(Common.DB_CART_ONSALE);

			realm.beginTransaction();
			cart = realm.copyToRealm(cart);
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public void addCart(Cart cart) {
		try {
			realm.beginTransaction();
			cart = realm.copyToRealm(cart);
			realm.commitTransaction();
		} catch (Exception e) {
			realm.cancelTransaction();
			e.printStackTrace();
		}
	}

	public void updateCart(int cartID, int quantity, double unitPrice, Callback callback) {
		this.callback = callback;

		try {
			ArrayList<Cart> carts = new ArrayList<>(realm.where(Cart.class).equalTo("id", cartID).findAll());
			if (carts.size() <= 0) {
				runOnFailure("No such cart found.");
				return;
			}

			realm.beginTransaction();
			Cart cart = carts.get(0);
			cart.setQuantity(quantity);
			cart.setUnitPrice(unitPrice);
			cart.setTotalPrice(quantity * unitPrice);
			cart.setSynced(false);
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public void cancelCart(Cart cart, Callback callback) {
		this.callback = callback;

		try {
			realm.beginTransaction();
			realm.where(Cart.class).equalTo("id", cart.getId()).and()
					.equalTo("status", Common.DB_CART_ONSALE)
					.findAll().deleteAllFromRealm();
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public void clearCarts(Callback callback) {
		this.callback = callback;

		try {
			realm.beginTransaction();
			realm.where(Cart.class).equalTo("status", Common.DB_CART_ONSALE)
					.findAll().deleteAllFromRealm();
			realm.commitTransaction();

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public ArrayList<Sale> reports(Date startTime, Date endTime) {
		ArrayList<Sale> sales = new ArrayList<>(realm.where(Sale.class).sort("date", Sort.DESCENDING).findAll());
		if (startTime == null || endTime == null)
			return sales;

		ArrayList<Sale> results = new ArrayList<>();
		for (Sale sale : sales) {
			if (sale.getDate().after(startTime) && sale.getDate().before(endTime)) {
				results.add(sale);
			} else if (sale.getDate().equals(startTime) || sale.getDate().equals(endTime)) {
				results.add(sale);
			}
		}

		return results;
	}

	public void addSale(Sale sale) {
		try {
			realm.beginTransaction();
			sale = realm.copyToRealm(sale);
			realm.commitTransaction();
		} catch (Exception e) {
			realm.cancelTransaction();
			e.printStackTrace();
		}
	}

	public void sale(ArrayList<Cart> carts, Callback callback) {
		this.callback = callback;

		try {
			RealmList<Cart> realmList = new RealmList<>();
			for (Cart cart : carts) {
				realmList.add(cart);
			}

			Sale sale = Sale.create();
			sale.setId(getAvailableSaleId());
			sale.setCarts(realmList);
			sale.setDate(Calendar.getInstance().getTime());
			sale.setStatus("");

			realm.beginTransaction();
			sale = realm.copyToRealm(sale);
			for (Cart cart : sale.getCarts()) {
				cart.setStatus(Common.DB_CART_SOLD);
				cart.setSynced(false);
			}
			realm.commitTransaction();

			for (Cart cart : sale.getCarts()) {
				saleProduct(cart.getProduct(), cart.getQuantity());
			}

			runOnSuccess();
		} catch (Exception e) {
			realm.cancelTransaction();
			runOnFailure(e.getMessage());
		}
	}

	public ArrayList<Product> productsNotSynced() {
		return new ArrayList<>(realm.where(Product.class).equalTo("synced", false).findAll());
	}

	public ArrayList<Inventory> inventoriesNotSynced() {
		return new ArrayList<>(realm.where(Inventory.class).equalTo("synced", false).findAll());
	}

	public ArrayList<Cart> cartsNotSynced() {
		return new ArrayList<>(realm.where(Cart.class).equalTo("synced", false).findAll());
	}

	public ArrayList<Sale> salesNotSynced() {
		return new ArrayList<>(realm.where(Sale.class).equalTo("synced", false).findAll());
	}

	public void markSynced(Class<RealmObject> cls, Integer id) {
		try {
			RealmObject object = realm.where(cls).equalTo("id", id).findFirst();
			if (object == null) {
				return;
			}

			realm.executeTransaction(new Realm.Transaction() {
				@Override
				public void execute(Realm realm) {
					try {
						Method[] methods = cls.getDeclaredMethods();
						for (Method method : methods) {
							if (method.getName().equals("setSynced")) {
								method.invoke(object, true);
								break;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			if (realm.isInTransaction()) realm.cancelTransaction();
			Log.e("Sync", e.getLocalizedMessage());
		}
	}
}
