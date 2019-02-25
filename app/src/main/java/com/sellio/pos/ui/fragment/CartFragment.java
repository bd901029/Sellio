package com.sellio.pos.ui.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.api.User;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.dialog.EditCartFragment;
import com.sellio.pos.ui.dialog.PaymentDialogFragment;

/**
 * UI for Sale operation.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class CartFragment extends UpdatableFragment {

	ReportFragment reportFragment;

	private TextView totalPrice;

	CartListViewAdapter cartListViewAdapter = new CartListViewAdapter();
	private ListView cartListView;

	ArrayList<Cart> carts = new ArrayList<>();

	public CartFragment(ReportFragment reportFragment) {
		super();

		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.layout_cart, container, false);

		initUI(view);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		update();
	}

	@Override
	public void update() {
		carts = DatabaseManager.sharedInstance().carts();

		cartListViewAdapter.notifyDataSetChanged();
		cartListView.invalidateViews();

		double priceSum = 0;
		for (Cart cart : carts) {
			priceSum += cart.getTotalPrice();
		}
		totalPrice.setText(String.valueOf(priceSum));
	}

	public void OnSaveBtnClicked() {
		if (carts.size() <= 0)
			return;

		PaymentDialogFragment fragment = new PaymentDialogFragment(carts, this, reportFragment);
		fragment.show(getFragmentManager(), "");
	}

	public void OnClearBtnClicked() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(getResources().getString(R.string.dialog_clear_sale));
		dialog.setPositiveButton(getResources().getString(R.string.no), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {}
		});

		dialog.setNegativeButton(getResources().getString(R.string.clear), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				DatabaseManager.sharedInstance().clearCarts(new DatabaseManager.Callback() {
					@Override
					public void OnSuccess() {
						update();
					}

					@Override
					public void OnFailure(String message) {
						((BaseFragmentActivity)getActivity()).showErrorMessage(message);
					}
				});
			}
		});

		dialog.show();
	}

	/**
	 * Initiate this UI.
	 */
	private void initUI(View view) {
		cartListView = view.findViewById(R.id.sale_List);
		cartListView.setAdapter(cartListViewAdapter);
		cartListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cart cart = carts.get(position);
				EditCartFragment fragment = new EditCartFragment(cart, CartFragment.this, reportFragment);
				fragment.show(getFragmentManager(), "");
			}
		});

		totalPrice = view.findViewById(R.id.totalPrice);

		Button btnClear = view.findViewById(R.id.btnClear);
		btnClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnClearBtnClicked();
			}
		});

		Button btnSave = view.findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnSaveBtnClicked();
			}
		});
	}

	private class CartListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return carts.size();
		}

		@Override
		public Object getItem(int position) {
			return carts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.listview_cart, null);
			}

			Cart cart = (Cart) getItem(position);

			TextView nameTextView = convertView.findViewById(R.id.nameTextView);
			nameTextView.setText(cart.getProduct().getName());

			TextView quantityTextView = convertView.findViewById(R.id.quantityTextView);
			quantityTextView.setText(String.valueOf(cart.getQuantity()));

			TextView unitPriceTextView = convertView.findViewById(R.id.unitPriceTextView);
			unitPriceTextView.setText(String.valueOf(cart.getUnitPrice()));

			TextView totalPriceTextView = convertView.findViewById(R.id.totalPriceTextView);
			totalPriceTextView.setText(String.valueOf(cart.getTotalPrice()));

			return convertView;
		}
	}
}
