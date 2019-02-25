package com.sellio.pos.ui.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.fragment.UpdatableFragment;

/**
 * A dialog for edit a LineItem of sale,
 * overriding price or set the quantity.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class EditCartFragment extends DialogFragment {

	Cart cart;
	private UpdatableFragment saleFragment;
	private UpdatableFragment reportFragment;

	private EditText quantityEditText;
	private EditText priceEditText;

	/**
	 * Construct a new  EditCartFragment.
	 * @param saleFragment
	 * @param reportFragment
	 */
	public EditCartFragment(Cart cart, UpdatableFragment saleFragment, UpdatableFragment reportFragment) {
		super();

		this.cart= cart;
		this.saleFragment = saleFragment;
		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.dialog_saleedit, container, false);

		initUI(view);

		return view;
	}
	
	void initUI(View view) {
		quantityEditText = view.findViewById(R.id.quantityEditText);
		quantityEditText.setText(String.valueOf(cart.getQuantity()));


		priceEditText = view.findViewById(R.id.priceBox);
		priceEditText.setText(String.valueOf(cart.getUnitPrice()));

		Button confirmButton = view.findViewById(R.id.btnSave);
		confirmButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				OnConfirmBtnClicked();
			}
		});

		Button removeButton = view.findViewById(R.id.removeButton);
		removeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				OnRemoveBtnClicked();
			}
		});
	}

	void OnConfirmBtnClicked() {
		String strQuantity = quantityEditText.getText().toString();
		String strPrice = priceEditText.getText().toString();
		if (strQuantity.equals("") || strPrice.equals("")) {
			((BaseFragmentActivity)getActivity()).showToast(R.string.please_input_all);
			return;
		}

		DatabaseManager.sharedInstance().updateCart(cart.getId(),
				Integer.parseInt(quantityEditText.getText().toString()),
				Double.parseDouble(priceEditText.getText().toString()),
				new DatabaseManager.Callback() {
					@Override
					public void OnSuccess() {
						saleFragment.update();
						reportFragment.update();

						dismiss();
					}

					@Override
					public void OnFailure(String message) {
						((BaseFragmentActivity)getActivity()).showErrorMessage(message);
					}
				});
	}

	void OnRemoveBtnClicked() {
		DatabaseManager.sharedInstance().cancelCart(cart, new DatabaseManager.Callback() {
			@Override
			public void OnSuccess() {
				saleFragment.update();
				reportFragment.update();

				dismiss();
			}

			@Override
			public void OnFailure(String message) {
				((BaseFragmentActivity)getActivity()).showErrorMessage(message);
			}
		});
	}
}
