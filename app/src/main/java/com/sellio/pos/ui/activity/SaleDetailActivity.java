package com.sellio.pos.ui.activity;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.Cart;
import com.sellio.pos.engine.db.Sale;
import com.sellio.pos.engine.Utility;

/**
 * UI for showing the detail of Sale in the record.
 * @author Refresh Team
 *
 */
public class SaleDetailActivity extends BaseActivity {

	public static Sale sale;

	private TextView totalPriceTextView;
	private TextView dateTextView;

	CartListViewAdapter cartListViewAdapter = new CartListViewAdapter();
	private ListView cartListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_saledetail);

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

		update();
	}

	private void initUI() {
		initiateActionBar();

		totalPriceTextView = findViewById(R.id.totalBox);
		dateTextView = findViewById(R.id.dateBox);

		cartListView = findViewById(R.id.lineitemList);
		cartListView.setAdapter(cartListViewAdapter);
	}

	private void initiateActionBar() {
		if (Utility.SDK_SUPPORTED) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(getResources().getString(R.string.sale));
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));
		}
	}

	void update() {
		totalPriceTextView.setText(sale.getTotalPrice() + "");
		dateTextView.setText(Utility.convertDateToText(sale.getDate()));

		cartListViewAdapter.notifyDataSetChanged();
		cartListView.invalidateViews();
	}

	private class CartListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return sale.getCarts().size();
		}

		@Override
		public Object getItem(int position) {
			return sale.getCarts().get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(SaleDetailActivity.this).inflate(R.layout.listview_lineitem, null);
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
