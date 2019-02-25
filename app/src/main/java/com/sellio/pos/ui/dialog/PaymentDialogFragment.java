package com.sellio.pos.ui.dialog;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.fragment.UpdatableFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A dialog for cashEditText a money for sale.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class PaymentDialogFragment extends DialogFragment {

	ArrayList<Cart> carts;

	private UpdatableFragment cartFragment;
	private UpdatableFragment reportFragment;

	private TextView totalPriceTextView;
	private EditText cashEditText;
	private Button btnClear;
	private Button btnSave;

	double totalPrice = 0;

	public PaymentDialogFragment(ArrayList<Cart> carts, UpdatableFragment cartFragment, UpdatableFragment reportFragment) {
		super();

		this.carts = carts;
		this.cartFragment = cartFragment;
		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.dialog_payment, container,false);

		initUI(view);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		update();
	}

	void OnSaveBtnClicked() {
		String strCash = cashEditText.getText().toString();
		if (strCash.equals("")) {
			((BaseFragmentActivity)getActivity()).showErrorToast(R.string.please_input_all);
			return;
		}

		double cash = Double.parseDouble(strCash);
		if (cash < totalPrice) {
			Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.need_money) + " " + (totalPrice-cash), Toast.LENGTH_SHORT).show();
		} else {
			double change = cash - totalPrice;
			EndPaymentDialogFragment newFragment = new EndPaymentDialogFragment(carts, cartFragment, reportFragment, change);
			newFragment.show(getFragmentManager(), "");
			dismiss();
		}
	}

	void OnCancelBtnClicked() {
		dismiss();
	}

	void initUI(View view) {
		totalPriceTextView = view.findViewById(R.id.payment_total);
		totalPriceTextView.setText(String.valueOf(totalPrice));

		cashEditText = view.findViewById(R.id.dialog_saleInput);

		btnClear = view.findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnCancelBtnClicked();
			}
		});

		btnSave = view.findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnSaveBtnClicked();
			}
		});
	}

	void update() {
		totalPrice = 0;
		for (Cart cart : carts) {
			totalPrice += cart.getTotalPrice();
		}
		totalPriceTextView.setText(String.valueOf(totalPrice));
	}
}
