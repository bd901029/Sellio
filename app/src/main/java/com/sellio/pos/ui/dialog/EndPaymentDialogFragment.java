package com.sellio.pos.ui.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.fragment.UpdatableFragment;

import java.util.ArrayList;

/**
 * A dialog shows the total change and confirmation for Sale.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class EndPaymentDialogFragment extends DialogFragment  {
	private UpdatableFragment cartFragment;
	private UpdatableFragment reportFragment;

	ArrayList<Cart> carts;
	double changes = 0;

	private Button btnDone;
	private TextView changesTextView;

	/**
	 * End this UI.
	 * @param cartFragment
	 * @param reportFragment
	 */
	public EndPaymentDialogFragment(ArrayList<Cart> carts, UpdatableFragment cartFragment, UpdatableFragment reportFragment, double changes) {
		super();

		this.carts = carts;
		this.cartFragment = cartFragment;
		this.reportFragment = reportFragment;
		this.changes = changes;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.dialog_paymentsuccession, container,false);

		initUI(view);

		return view;
	}

	void OnDoneBtnClicked() {
		DatabaseManager.sharedInstance().sale(carts, new DatabaseManager.Callback() {
			@Override
			public void OnSuccess() {
				cartFragment.update();
				reportFragment.update();
				dismiss();
			}

			@Override
			public void OnFailure(String message) {
				((BaseFragmentActivity)getActivity()).showErrorMessage(message);
			}
		});

	}

	void initUI(View view) {
		changesTextView = view.findViewById(R.id.changeTxt);
		changesTextView.setText(String.valueOf(changes));

		btnDone = view.findViewById(R.id.doneButton);
		btnDone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnDoneBtnClicked();
			}
		});
	}
}
