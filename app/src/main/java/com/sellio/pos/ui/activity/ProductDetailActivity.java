package com.sellio.pos.ui.activity;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.api.User;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.DateTimeStrategy;
import com.sellio.pos.engine.db.Inventory;
import com.sellio.pos.engine.db.Product;
import com.sellio.pos.engine.Utility;

import java.util.ArrayList;
import java.util.Calendar;

import static com.sellio.pos.engine.Common.SortType.DAILY;
import static com.sellio.pos.engine.Common.SortType.MONTHLY;
import static com.sellio.pos.engine.Common.SortType.WEEKLY;
import static com.sellio.pos.engine.Common.SortType.YEARLY;

public class ProductDetailActivity extends BaseActivity {
	public static Product product = null;

	TabHost tabHost;

	Button btnProductModify;

	ArrayList<EditText> arrProductDetailTextViews = new ArrayList<>();
	EditText productNameTextView;
	EditText productCostTextView;
	EditText productPriceTextView;
	EditText productCategoryTextView;
	EditText productUnitTextView;
	EditText productBarcodeTextView;
	ImageView productImageView;

	LinearLayout productDetailBottomButtonContainer = null;

	Spinner sortSpinner;
	TextView currentTimeTextView;
	ListView stockListView; StockListViewAdapter stockListViewAdapter;

	ArrayList<Inventory> inventories = new ArrayList<>();
	private Calendar currentTime = Calendar.getInstance(), startTime = null, endTime = null;

	String productPhotoPath = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_productdetail_main);

		initUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateProductDetail();
		updateInventoryDetail();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == Utility.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
			Uri selectedImage = intent.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			productPhotoPath = cursor.getString(columnIndex);
			cursor.close();
		}
	}

	public void OnProductDetailEditBtnClicked(View view) {
		if (!User.sharedInstance().isAdmin())
			return;

		btnProductModify.setVisibility(View.GONE);

		productDetailBottomButtonContainer.setVisibility(View.VISIBLE);

		int bgColor = Color.parseColor("#87CEEB");
		for (EditText editText : arrProductDetailTextViews) {
			editText.setBackgroundColor(bgColor);
			editText.setFocusable(true);
			editText.setFocusableInTouchMode(true);
		}
	}

	public void OnProductDetailDeleteBtnClicked(View view) {
		new AlertDialog.Builder(this)
				.setTitle(null)
				.setMessage("Are you sure want to delete this product?")
				.setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						DatabaseManager.sharedInstance().deleteProduct(product, new DatabaseManager.Callback() {
							@Override
							public void OnSuccess() {
								finish();
							}

							@Override
							public void OnFailure(String message) {
								showErrorMessage(message);
							}
						});
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.show();
	}

	public void OnProductDetailCancelBtnClicked(View view) {
		btnProductModify.setVisibility(View.VISIBLE);

		productDetailBottomButtonContainer.setVisibility(View.GONE);

		int bgColor = Color.parseColor("#EEEEEE");
		for (EditText editText : arrProductDetailTextViews) {
			editText.setBackgroundColor(bgColor);
			editText.setFocusable(false);
			editText.setFocusableInTouchMode(false);
		}

		hideSoftKeyboard();
	}

	public void OnProductDetailSaveBtnClicked(View view) {
		hideSoftKeyboard();

		new AlertDialog.Builder(this)
				.setTitle(null)
				.setMessage("Are you sure save current changes?")
				.setPositiveButton(getResources().getText(R.string.save), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Product newProduct = Product.create(product.getId(),
								productNameTextView.getText().toString(),
								productBarcodeTextView.getText().toString(),
								productUnitTextView.getText().toString(),
								Float.parseFloat(productCostTextView.getText().toString()),
								Float.parseFloat(productPriceTextView.getText().toString()),
								productCategoryTextView.getText().toString(),
								(productPhotoPath == null || productPhotoPath.equals("")) ? product.getPhoto() : productPhotoPath);
						DatabaseManager.sharedInstance().updateProduct(newProduct, new DatabaseManager.Callback() {
							@Override
							public void OnSuccess() {
								showMessage("Success", "This product changes has been saved successfully.");
							}

							@Override
							public void OnFailure(String message) {
								showErrorMessage(message);
							}
						});
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showSoftKeyboard(productNameTextView);
					}
				})
				.show();
	}

	public void OnProductDetailImageBtnClicked(View view) {
		if (!User.sharedInstance().isAdmin())
			return;

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

			Utility.requestStoragePermission(this);
			//restart();
		}

		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, Utility.RESULT_LOAD_IMAGE);
	}

	public void OnPrevTimeBtnClicked(View view) {
		addDate(-1);
	}

	public void OnNextTimeBtnClicked(View view) {
		addDate(1);
	}

	public void OnCurrentTimeTextViewClicked(View view) {
		DatePickerDialog datePickerDlg = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int y, int m, int d) {
				currentTime.set(Calendar.YEAR, y);
				currentTime.set(Calendar.MONTH, m);
				currentTime.set(Calendar.DAY_OF_MONTH, d);

				updateInventoryDetail();
			}
		}, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		datePickerDlg.show();
	}

	public void OnAddStocksBtnClicked(View view) {
		if (!User.sharedInstance().isAdmin())
			return;

		AddStockActivity.product = product;
		Intent intent = new Intent(this, AddStockActivity.class);
		startActivity(intent);
	}

	private void initUI() {
		if (Utility.SDK_SUPPORTED) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(getResources().getString(R.string.product_detail));
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009999")));
		}

		tabHost = findViewById(android.R.id.tabhost);
		tabHost.setup();
		tabHost.addTab(tabHost.newTabSpec("tab_test1").setIndicator(getResources().getString(R.string.product_detail))
				.setContent(R.id.tab1));
		tabHost.addTab(tabHost.newTabSpec("tab_test2").setIndicator(getResources().getString(R.string.stocks))
				.setContent(R.id.tab2));
		tabHost.setCurrentTab(0);

		btnProductModify = findViewById(R.id.btnProductModify);

		productNameTextView = findViewById(R.id.productNameEditText);
		productCostTextView = findViewById(R.id.productCostEditText);
		productPriceTextView = findViewById(R.id.productPriceEditText);
		productCategoryTextView = findViewById(R.id.productCategoryEditText);
		productUnitTextView = findViewById(R.id.productUnitEditText);
		productBarcodeTextView = findViewById(R.id.productBarcodeEditText);
		productImageView = findViewById(R.id.productImageView);

		arrProductDetailTextViews.add(productNameTextView);
		arrProductDetailTextViews.add(productCostTextView);
		arrProductDetailTextViews.add(productPriceTextView);
		arrProductDetailTextViews.add(productCategoryTextView);
		arrProductDetailTextViews.add(productUnitTextView);
		arrProductDetailTextViews.add(productBarcodeTextView);

		productDetailBottomButtonContainer = findViewById(R.id.bottomButtonContainer);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getBaseContext(),
				R.array.period, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortSpinner = findViewById(R.id.spinnerSort);
		sortSpinner.setAdapter(adapter);
		sortSpinner.setSelection(2);
		sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				updateInventoryDetail();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		currentTimeTextView = findViewById(R.id.currentTimeTextView);
		stockListView = findViewById(R.id.inventoryListView);

		updateProductDetail();
		updateInventoryDetail();
	}

	void updateProductDetail() {
		productNameTextView.setText(product.getName());
		productCostTextView.setText(String.valueOf(product.getCostPrice()));
		productPriceTextView.setText(String.valueOf(product.getSalePrice()));
		productCategoryTextView.setText(product.getCategory());
		productUnitTextView.setText(product.getUnit());
		productBarcodeTextView.setText(product.getBarcode());

		productImageView.setBackgroundResource(0);
		if (productPhotoPath != null || product.hasPhoto()) {
			productImageView.setImageBitmap((productPhotoPath != null) ? Utility.bitmapFromFile(productPhotoPath) : product.getPhotoBitmap());
		} else {
			productImageView.setBackgroundResource(R.drawable.picture);
		}
	}

	void updateInventoryDetail() {
		startTime = (Calendar) currentTime.clone();
		endTime = (Calendar) currentTime.clone();

		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);

		endTime.set(Calendar.HOUR_OF_DAY, 23);
		endTime.set(Calendar.MINUTE, 59);
		endTime.set(Calendar.SECOND, 59);

		int period = sortSpinner.getSelectedItemPosition();
		if (period == DAILY.getInt()) {
			currentTimeTextView.setText(" [" + DateTimeStrategy.getSQLDateFormat(currentTime) +  "] ");
			currentTimeTextView.setTextSize(16);
		} else if (period == WEEKLY.getInt()){
			while(startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				startTime.add(Calendar.DATE, -1);
			}

			String toShow = " [" + DateTimeStrategy.getSQLDateFormat(startTime) +  "] ~ [";
			endTime = (Calendar) startTime.clone();
			endTime.add(Calendar.DATE, 7);
			toShow += DateTimeStrategy.getSQLDateFormat(endTime) +  "] ";
			currentTimeTextView.setTextSize(16);
			currentTimeTextView.setText(toShow);
		} else if (period == MONTHLY.getInt()){
			startTime.set(Calendar.DATE, 1);
			endTime = (Calendar) startTime.clone();

			endTime.add(Calendar.MONTH, 1);
			endTime.add(Calendar.DATE, -1);

			currentTimeTextView.setTextSize(18);
			currentTimeTextView.setText(" [" + currentTime.get(Calendar.YEAR) + "-" + (currentTime.get(Calendar.MONTH)+1) + "] ");
		} else if (period == YEARLY.getInt()){
			startTime.set(Calendar.DATE, 1);
			startTime.set(Calendar.MONTH, 0);

			endTime = (Calendar) startTime.clone();
			endTime.add(Calendar.YEAR, 1);
			endTime.add(Calendar.DATE, -1);
			currentTimeTextView.setTextSize(20);
			currentTimeTextView.setText(" [" + currentTime.get(Calendar.YEAR) +  "] ");
		}
		currentTime = startTime;

		inventories = DatabaseManager.sharedInstance().inventoriesByProduct(product, startTime, endTime);
		if (stockListViewAdapter == null) {
			stockListViewAdapter = new StockListViewAdapter();
			stockListView.setAdapter(stockListViewAdapter);
		} else {
			stockListViewAdapter.notifyDataSetChanged();
			stockListView.invalidateViews();
		}
	}

	private void addDate(int offset) {
		int period = sortSpinner.getSelectedItemPosition();
		if (period == DAILY.getInt()){
			currentTime.add(Calendar.DATE, 1 * offset);
		} else if (period == WEEKLY.getInt()){
			currentTime.add(Calendar.DATE, 7 * offset);
		} else if (period == MONTHLY.getInt()){
			currentTime.add(Calendar.MONTH, 1 * offset);
		} else if (period == YEARLY.getInt()){
			currentTime.add(Calendar.YEAR, 1 * offset);
		}

		updateInventoryDetail();
	}

	private class StockListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return inventories.size();
		}

		@Override
		public Object getItem(int position) {
			return inventories.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(ProductDetailActivity.this).inflate(R.layout.listview_stock, null);
			}

			Inventory inventory = (Inventory) getItem(position);

			Log.v("inventory", String.valueOf(inventory.getCostPrice()));

			TextView dateAddedTextView = convertView.findViewById(R.id.dateAddedTextView);
			dateAddedTextView.setText(Utility.convertDateToText(inventory.getDateAdded()));

			TextView modeTextView = convertView.findViewById(R.id.modeTextView);
			modeTextView.setText(inventory.getMode());

			TextView costTextView = convertView.findViewById(R.id.costPriceTextView);
			costTextView.setText(String.valueOf(inventory.getCostPrice()));

			TextView quantityTextView = convertView.findViewById(R.id.quantityTextView);
			quantityTextView.setText(String.valueOf(inventory.getQuantity()));

			TextView balanceTextView = convertView.findViewById(R.id.balanceTextView);
			balanceTextView.setText(String.valueOf(inventory.getBalance()));

			return convertView;
		}
	}
}