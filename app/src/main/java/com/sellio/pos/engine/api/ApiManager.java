package com.sellio.pos.engine.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.sellio.pos.SellioApplication;
import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.Utility;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.db.Inventory;
import com.sellio.pos.engine.db.Product;
import com.sellio.pos.engine.db.Sale;
import com.sellio.pos.ui.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import io.realm.RealmObject;

public class ApiManager extends BaseApiManager {

	private static ApiManager instance = null;
	public static ApiManager sharedInstance() {
		if (instance == null)
			instance = new ApiManager();
		return instance;
	}

	boolean stopped = false;
	Thread thread = null;

	private void invokeInMainThread(String funcName, Class cls, Integer id) {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					Method[] methods = DatabaseManager.class.getDeclaredMethods();
					for (Method method : methods) {
						if (method.getName().equals(funcName)) {
							method.invoke(DatabaseManager.sharedInstance(), cls, id);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void invokeInMainThread(String funcName, RealmObject object) {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					Method[] methods = DatabaseManager.class.getDeclaredMethods();
					for (Method method : methods) {
						if (method.getName().equals(funcName)) {
							method.invoke(DatabaseManager.sharedInstance(), object);
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ApiManager() {
	}

	public void start() {
		stop();

		stopped = false;

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					while (!stopped) {
						Thread.sleep(1000 * 10);

						mainProc();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		thread = new Thread(runnable);
		thread.start();
	}

	public void stop() {
		stopped = true;
	}

	private void mainProc() {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (User.sharedInstance().isAdmin()) {
					syncProduct();
				}

				syncInventory();
				syncCart();
				syncSale();
			}
		});
	}

	private void syncProduct() {
		ArrayList<Product> products = DatabaseManager.sharedInstance().productsNotSynced();
		Log.i("ApiManager", "Found " + products.size() + " products not synced.");
		new SyncProductAsyncTack(products).execute();
	}

	private void syncInventory() {
		ArrayList<Inventory> inventories = DatabaseManager.sharedInstance().inventoriesNotSynced();
		Log.i("ApiManager", "Found " + inventories.size() + " inventories not synced.");
		new SyncInventoryAsyncTack(inventories).execute();
	}

	private void syncCart() {
		ArrayList<Cart> carts = DatabaseManager.sharedInstance().cartsNotSynced();
		Log.i("ApiManager", "Found " + carts.size() + " carts not synced.");
		new SyncCartAsyncTack(carts).execute();
	}

	private void syncSale() {
		ArrayList<Sale> sales = DatabaseManager.sharedInstance().salesNotSynced();
		Log.i("ApiManager", "Found " + sales.size() + " apiSales not synced.");
		new SyncSaleAsyncTack(sales).execute();
	}

	private void syncProduct(Product.ApiProduct product) {
		try {
			String strUrl = "http://sellio.posmart.co.ke/api/syncProduct.php";

			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addTextBody("id", String.valueOf(product.id));
			entityBuilder.addTextBody("name", product.name);
			entityBuilder.addTextBody("barcode", product.barcode);
			entityBuilder.addTextBody("unit", product.unit);
			entityBuilder.addTextBody("cost", String.valueOf(product.costPrice));
			entityBuilder.addTextBody("price", String.valueOf(product.salePrice));
			entityBuilder.addTextBody("category", product.category);
			entityBuilder.addTextBody("deleted", String.valueOf(product.deleted));
			if (product.hasPhoto()) {
				File file = new File(product.photo);
				String fileName = file.getName();
				ContentType contentType = ContentType.MULTIPART_FORM_DATA;
				FileBody fileBody = new FileBody(file, contentType, fileName);
				entityBuilder.addPart("photo", fileBody);
			}

			HttpEntity httpEntity = entityBuilder.build();
			HttpPost request = new HttpPost(strUrl);
			request.setEntity(httpEntity);
			HttpResponse resp = new DefaultHttpClient().execute(request);
			String strResponse = EntityUtils.toString(resp.getEntity()).trim();
			JSONObject jsonObject = new JSONObject(strResponse);

			boolean isSucceed = jsonObject.getBoolean("success");
			if (isSucceed) {
				invokeInMainThread("markSynced", Product.class, product.id);

				Log.i("ApiManager", "Product #" + product.id + " has been synced successfully.");
			} else {
				Log.i("ApiManager", "Product #" + product.id + " has not been synced.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void syncInventory(Inventory.ApiInventory inventory) {
		try {
			String strUrl = "http://sellio.posmart.co.ke/api/syncInventory.php";

			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addTextBody("id", String.valueOf(inventory.id));
			entityBuilder.addTextBody("product_id", String.valueOf(inventory.productID));
			entityBuilder.addTextBody("cost", String.valueOf(inventory.cost));
			entityBuilder.addTextBody("mode", inventory.mode);
			entityBuilder.addTextBody("date", Utility.convertDateToText(inventory.date));
			entityBuilder.addTextBody("initial", String.valueOf(inventory.initial));
			entityBuilder.addTextBody("quantity", String.valueOf(inventory.quantity));
			entityBuilder.addTextBody("balance", String.valueOf(inventory.balance));
			entityBuilder.addTextBody("name", inventory.name);
			entityBuilder.addTextBody("status", inventory.status);

			HttpEntity httpEntity = entityBuilder.build();
			HttpPost request = new HttpPost(strUrl);
			request.setEntity(httpEntity);
			HttpResponse resp = new DefaultHttpClient().execute(request);
			String strResponse = EntityUtils.toString(resp.getEntity()).trim();
			JSONObject jsonObject = new JSONObject(strResponse);

			boolean isSucceed = jsonObject.getBoolean("success");
			if (isSucceed) {
				invokeInMainThread("markSynced", Inventory.class, inventory.id);

				Log.i("ApiManager", "Inventory #" + inventory.id + " has been synced successfully.");
			} else {
				Log.i("ApiManager", "Inventory #" + inventory.id + " has not been synced.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void syncCart(Cart.ApiCart cart) {
		try {
			String strUrl = "http://sellio.posmart.co.ke/api/syncCart.php";

			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addTextBody("id", String.valueOf(cart.id));
			entityBuilder.addTextBody("product_id", String.valueOf(cart.productID));
			entityBuilder.addTextBody("quantity", String.valueOf(cart.quantity));
			entityBuilder.addTextBody("unitPrice", String.valueOf(cart.unitPrice));
			entityBuilder.addTextBody("totalPrice", String.valueOf(cart.totalPrice));
			entityBuilder.addTextBody("status", cart.status);

			HttpEntity httpEntity = entityBuilder.build();
			HttpPost request = new HttpPost(strUrl);
			request.setEntity(httpEntity);
			HttpResponse resp = new DefaultHttpClient().execute(request);
			String strResponse = EntityUtils.toString(resp.getEntity()).trim();
			JSONObject jsonObject = new JSONObject(strResponse);

			boolean isSucceed = jsonObject.getBoolean("success");
			if (isSucceed) {
				invokeInMainThread("markSynced", Cart.class, cart.id);

				Log.i("ApiManager", "Cart #" + cart.id + " has been synced successfully.");
			} else {
				Log.i("ApiManager", "Cart #" + cart.id + " has not been synced.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void syncSale(Sale.ApiSale sale) {
		try {
			String strUrl = "http://sellio.posmart.co.ke/api/syncSale.php";

			MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
			entityBuilder.addTextBody("id", String.valueOf(sale.id));
			entityBuilder.addTextBody("cartInfo", sale.cartInfo);
			entityBuilder.addTextBody("date", Utility.convertDateToText(sale.date));
			entityBuilder.addTextBody("status", sale.status);

			HttpEntity httpEntity = entityBuilder.build();
			HttpPost request = new HttpPost(strUrl);
			request.setEntity(httpEntity);
			HttpResponse resp = new DefaultHttpClient().execute(request);
			String strResponse = EntityUtils.toString(resp.getEntity()).trim();
			JSONObject jsonObject = new JSONObject(strResponse);

			boolean isSucceed = jsonObject.getBoolean("success");
			if (isSucceed) {
				invokeInMainThread("markSynced", Sale.class, sale.id);

				Log.i("ApiManager", "Sale #" + sale.id + " has been synced successfully.");
			} else {
				Log.i("ApiManager", "Sale #" + sale.id + " has not been synced.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class SyncProductAsyncTack extends AsyncTask {
		ArrayList<Product.ApiProduct> products = new ArrayList<>();
		public SyncProductAsyncTack(ArrayList<Product> param) {
			this.products = new ArrayList<>();
			for (Product product : param) {
				this.products.add(Product.ApiProduct.from(product));
			}
		}

		@Override
		protected Object doInBackground(Object[] objects) {

			for (Product.ApiProduct product : products) {
				syncProduct(product);
			}

			return null;
		}
	}

	private class SyncInventoryAsyncTack extends AsyncTask {
		ArrayList<Inventory.ApiInventory> inventories = new ArrayList<>();
		public SyncInventoryAsyncTack(ArrayList<Inventory> param) {
			this.inventories = new ArrayList<>();
			for (Inventory inventory : param) {
				this.inventories.add(Inventory.ApiInventory.from(inventory));
			}
		}

		@Override
		protected Object doInBackground(Object[] objects) {

			for (Inventory.ApiInventory inventory : inventories) {
				syncInventory(inventory);
			}

			return null;
		}
	}

	private class SyncCartAsyncTack extends AsyncTask {
		ArrayList<Cart.ApiCart> carts = new ArrayList<>();
		public SyncCartAsyncTack(ArrayList<Cart> param) {
			this.carts = new ArrayList<>();
			for (Cart cart : param) {
				this.carts.add(Cart.ApiCart.from(cart));
			}
		}

		@Override
		protected Object doInBackground(Object[] objects) {

			for (Cart.ApiCart cart : carts) {
				syncCart(cart);
			}

			return null;
		}
	}

	private class SyncSaleAsyncTack extends AsyncTask {
		ArrayList<Sale.ApiSale> sales = new ArrayList<>();
		public SyncSaleAsyncTack(ArrayList<Sale> param) {
			this.sales = new ArrayList<>();
			for (Sale sale : param) {
				this.sales.add(Sale.ApiSale.from(sale));
			}
		}

		@Override
		protected Object doInBackground(Object[] objects) {

			for (Sale.ApiSale sale : sales) {
				syncSale(sale);
			}

			return null;
		}
	}

	public void downloadAllExistingInformation(Callback callback) {
		this.callback = callback;

		new GetAllAsyncTack().execute();
	}

	private class GetAllAsyncTack extends AsyncTask {
		ArrayList<Product.ApiProduct> apiProducts = new ArrayList<>();
		ArrayList<Inventory.ApiInventory> apiInventories = new ArrayList<>();
		ArrayList<Cart.ApiCart> apiCarts = new ArrayList<>();
		ArrayList<Sale.ApiSale> apiSales = new ArrayList<>();
		@Override
		protected void onPreExecute() {}

		@Override
		protected JSONObject doInBackground(Object[] objects) {

			try {
				String strUrl = "http://sellio.posmart.co.ke/api/getAll.php";

				HttpPost request = new HttpPost(strUrl);
				HttpResponse resp = new DefaultHttpClient().execute(request);
				String strResponse = EntityUtils.toString(resp.getEntity()).trim();
				JSONObject jsonObject = new JSONObject(strResponse);

				boolean isSucceed = jsonObject.getBoolean("success");
				if (isSucceed) {
					if (jsonObject.isNull("info")) {
						runOnSuccess();
						return null;
					}

					JSONObject info = jsonObject.getJSONObject("info");
					JSONArray productInfos = info.getJSONArray("product");
					for (int i = 0; i < productInfos.length(); i++) {
						JSONObject productInfo = (JSONObject) productInfos.get(i);
						apiProducts.add(Product.ApiProduct.from(productInfo));
					}

					JSONArray inventoryInfos = info.getJSONArray("inventory");
					for (int i = 0; i < inventoryInfos.length(); i++) {
						JSONObject inventoryInfo = (JSONObject) inventoryInfos.get(i);
						apiInventories.add(Inventory.ApiInventory.from(inventoryInfo));
					}

					JSONArray cartInfos = info.getJSONArray("cart");
					for (int i = 0; i < cartInfos.length(); i++) {
						JSONObject cartInfo = (JSONObject) cartInfos.get(i);
						apiCarts.add(Cart.ApiCart.from(cartInfo));
					}

					JSONArray saleInfos = info.getJSONArray("sale");
					for (int i = 0; i < saleInfos.length(); i++) {
						JSONObject saleInfo = (JSONObject) saleInfos.get(i);
						apiSales.add(Sale.ApiSale.create(saleInfo));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				runOnFailure(e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			for (Product.ApiProduct apiProduct : apiProducts) {
				DatabaseManager.sharedInstance().addProduct(apiProduct.product());
			}

			for (Inventory.ApiInventory apiInventory : apiInventories) {
				DatabaseManager.sharedInstance().addInventory(apiInventory.inventory());
			}

			for (Cart.ApiCart apiCart : apiCarts) {
				DatabaseManager.sharedInstance().addCart(apiCart.cart());
			}

			for (Sale.ApiSale apiSale : apiSales) {
				DatabaseManager.sharedInstance().addSale(apiSale.sale());
			}

			runOnSuccess();
		}
	}
}
