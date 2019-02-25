package com.sellio.pos.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.db.Product;

/**
 * A dialog of adding a Product.
 * 
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class AddStockActivity extends BaseActivity {

	public static Product product = null;

	EditText costEditText, quantityEditText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_addstock);

		initUI();
	}

	public void OnSaveBtnClicked() {
		String strCost = costEditText.getText().toString();
		String strQuantity = quantityEditText.getText().toString();

		if (strCost.equals("") || strQuantity.equals("")) {
			showErrorToast(R.string.please_input_all);
			return;
		}

		DatabaseManager.sharedInstance().purchaseProduct(product, Double.parseDouble(strCost), Integer.parseInt(strQuantity), new DatabaseManager.Callback() {
			@Override
			public void OnSuccess() {
				showToast(R.string.success);
				finish();
			}

			@Override
			public void OnFailure(String message) {
				showErrorMessage(message);
			}
		});
	}

	public void OnClearBtnClicked() {
		String strCost = costEditText.getText().toString();
		String strQuantity = quantityEditText.getText().toString();

		if (strCost.equals("") && strQuantity.equals("")) {
			finish();
			return;
		}

		costEditText.setText("");
		quantityEditText.setText("");
	}

	private void initUI() {
		costEditText = findViewById(R.id.costEditText);
		quantityEditText = findViewById(R.id.quantityEditText);

		if (product != null) {
			costEditText.setText(String.valueOf(product.getCostPrice()));
		}

		Button btnSave = findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnSaveBtnClicked();
			}
		});

		Button btnClear = findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnClearBtnClicked();
			}
		});

		quantityEditText.setText("5");
	}
}
