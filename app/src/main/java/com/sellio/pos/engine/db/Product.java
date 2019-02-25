package com.sellio.pos.engine.db;

import android.graphics.Bitmap;
import android.os.Environment;

import com.sellio.pos.engine.Utility;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Product or item represents the real product in store.
 *
 * @author Refresh Team
 *
 */
public class Product extends RealmObject {
	@PrimaryKey private int id;
	private String name;
	private String barcode;
	private double costPrice;
	private String unit;
	private double salePrice;
	private String category;
	private String photo;
	private boolean deleted;
	private boolean synced;

	public Product() {
		synced = false;
		deleted = false;
	}

	public static Product create() {
		return new Product();
	}

	public static Product create(int id,
								 String name,
								 String barcode,
								 String unit,
								 double costPrice,
								 double salePrice,
								 String category,
								 String photo) {
		Product product = new Product();

		product.id = id;
		product.name = name;
		product.barcode = barcode;
		product.unit = unit;
		product.costPrice = costPrice;
		product.salePrice = salePrice;
		product.category =  category;
		product.photo = photo;

		return product;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public double getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(double costPrice) {
		this.costPrice = costPrice;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPhoto() {
		return photo;
	}

	public Bitmap getPhotoBitmap() {
		return Utility.bitmapFromFile(photo);
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isSynced() {
		return synced;
	}

	public void setSynced(boolean synced) {
		this.synced = synced;
	}

	public boolean hasPhoto() {
		return (photo != null && !photo.equals(""));
	}

	public static class ApiProduct {
		public int id;
		public String name;
		public String barcode;
		public double costPrice;
		public String unit;
		public double salePrice;
		public String category;
		public String photo;
		public boolean deleted;

		public ApiProduct() {}

		public static ApiProduct from(Product product) {
			ApiProduct apiProduct = new ApiProduct();

			apiProduct.id = product.getId();
			apiProduct.name = product.getName();
			apiProduct.barcode = product.getBarcode();
			apiProduct.costPrice = product.getCostPrice();
			apiProduct.unit = product.getUnit();
			apiProduct.salePrice = product.getSalePrice();
			apiProduct.category = product.getCategory();
			apiProduct.photo = product.getPhoto();
			apiProduct.deleted = product.isDeleted();

			return apiProduct;
		}

		public static ApiProduct from(JSONObject info) {
			ApiProduct product = new ApiProduct();
			try {
				product.id = info.getInt("id");
				product.name = info.getString("name");
				product.barcode = info.getString("barcode");
				product.unit = info.getString("unit");
				product.costPrice = info.getDouble("cost");
				product.salePrice = info.getDouble("price");
				product.category = info.getString("category");
				product.deleted = info.getInt("deleted") == 1;

				String photo = info.getString("photo");
				if (photo != null && !photo.equals("")) {
					String photoUrl = "http://sellio.posmart.co.ke/api/image/" + photo;
					String folderPath = Environment.getExternalStorageDirectory() + "/sellio";

					File folder = new File(folderPath);
					if (!folder.exists()) {
						folder.mkdir();
					}

					String filePath = folderPath + "/" + photo;
					URL url = new URL (photoUrl);
					InputStream input = url.openStream();
					OutputStream output = new FileOutputStream(filePath);
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
						output.write(buffer, 0, bytesRead);
					}
					output.close();
					input.close();

					product.photo = filePath;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return product;
		}

		public Product product() {
			Product product = Product.create();

			product.id = id;
			product.name = name;
			product.barcode = barcode;
			product.unit = unit;
			product.costPrice = costPrice;
			product.salePrice = salePrice;
			product.category = category;
			product.photo = photo;
			product.deleted = deleted;

			return product;
		}

		public boolean hasPhoto() {
			return (photo != null && !photo.equals("") && new File(photo).exists());
		}
	}
}
