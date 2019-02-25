package com.sellio.pos.ui.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.sellio.pos.R;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.db.Product;
import com.sellio.pos.engine.Utility;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.fragment.UpdatableFragment;

import static android.app.Activity.RESULT_OK;

/**
 * A dialog of adding a Product.
 * 
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class AddProductDialogFragment extends DialogFragment {

	private EditText barcodeBox;
	private Button barcodeBtn;
	private EditText costPriceBox;
	private EditText unitBox;
	private EditText salePriceBox;
	private EditText nameBox;
	private EditText categoryBox;
	private ImageView productImageBox;
	private Button saveBtn;
	private Button clearBtn;
	private UpdatableFragment fragment;

	private String productPhotoPath = "";

	/**
	 * Construct a new AddProductDialogFragment
	 * @param fragment
	 */
	public AddProductDialogFragment(UpdatableFragment fragment) {
		super();
		this.fragment = fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.layout_addproduct, container, false);

		initUI(v);

		return v;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == Utility.RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
			Uri selectedImage = intent.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getActivity().getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			productPhotoPath = cursor.getString(columnIndex);
			cursor.close();
			productImageBox.setBackgroundResource(0);
			productImageBox.setImageBitmap(Utility.bitmapFromFile(productPhotoPath));
		}
		else {
			IntentResult scanningResult = IntentIntegrator.parseActivityResult(
					requestCode, resultCode, intent);

			if (scanningResult != null) {
				String scanContent = scanningResult.getContents();
				barcodeBox.setText(scanContent);
			} else {
				Toast.makeText(getActivity().getBaseContext(),
						getResources().getString(R.string.fail),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void OnBarcodeBtnClicked() {
		IntentIntegratorSupportV4 scanIntegrator = new IntentIntegratorSupportV4(AddProductDialogFragment.this);
		scanIntegrator.initiateScan();
	}

	public void OnPhotoBtnClicked() {
		if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			Utility.requestStoragePermission(getActivity());
		}

		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, Utility.RESULT_LOAD_IMAGE);
	}

	public void OnSaveBtnClicked() {
		if (nameBox.getText().toString().equals("")
				|| barcodeBox.getText().toString().equals("")
				|| costPriceBox.getText().toString().equals("")
				|| unitBox.getText().toString().equals("")
				|| salePriceBox.getText().toString().equals("")
				|| categoryBox.getText().toString().equals("")) {

			Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.please_input_all), Toast.LENGTH_SHORT)
					.show();
			return;
		}

		Product product = new Product();
		product.setName(nameBox.getText().toString());
		product.setBarcode(barcodeBox.getText().toString());
		product.setUnit(unitBox.getText().toString());
		product.setCostPrice(Float.parseFloat(costPriceBox.getText().toString()));
		product.setSalePrice(Float.parseFloat(salePriceBox.getText().toString()));
		product.setCategory(categoryBox.getText().toString());
		product.setPhoto(productPhotoPath);

		BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
		activity.showProgress();
		DatabaseManager.sharedInstance().addProduct(product, new DatabaseManager.Callback() {
			@Override
			public void OnSuccess() {
				activity.hideProgress();
				dismiss();
				fragment.update();
			}

			@Override
			public void OnFailure(String message) {
				activity.hideProgress();

				activity.showErrorMessage(message);
			}
		});
	}

	public void OnClearBtnClicked() {
		if (barcodeBox.getText().toString().equals("") && nameBox.getText().toString().equals("") && costPriceBox.getText().toString().equals("")
				&& unitBox.getText().toString().equals("") && salePriceBox.getText().toString().equals("")
				&& categoryBox.getText().toString().equals("")){
			AddProductDialogFragment.this.dismiss();
		} else {
			clearAllBox();
		}
	}

	private void initUI(View v) {
//		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.image);
//		productImageBox.setBackground(getResources().getDrawable(R.drawable.monkey));

		barcodeBox = v.findViewById(R.id.barcodeBox);
		barcodeBtn = v.findViewById(R.id.btnBarcode);
		costPriceBox = v.findViewById(R.id.productCostEditText);
		unitBox = v.findViewById(R.id.unitBox);
		salePriceBox = v.findViewById(R.id.productPriceEditText);
		categoryBox = v.findViewById(R.id.productCategoryEditText);
		nameBox = v.findViewById(R.id.nameBox);
		productImageBox = v.findViewById(R.id.productImageView);

		saveBtn = v.findViewById(R.id.btnSave);
		clearBtn = v.findViewById(R.id.btnClear);

		productImageBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				OnPhotoBtnClicked();
			}
		});

		barcodeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnBarcodeBtnClicked();
			}
		});

		saveBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				OnSaveBtnClicked();
			}
		});
		
		clearBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnClearBtnClicked();
			}
		});

		nameBox.setText("Beige Ankle Shoes");
		unitBox.setText("Piece");
		costPriceBox.setText("1500.0");
		salePriceBox.setText("2000.0");
		categoryBox.setText("Ladies Shoes");
		barcodeBox.setText("800012122");
	}

	private void clearAllBox() {
		barcodeBox.setText("");
		nameBox.setText("");
		costPriceBox.setText("");
		unitBox.setText("");
		salePriceBox.setText("");
		categoryBox.setText("");
	}
}
