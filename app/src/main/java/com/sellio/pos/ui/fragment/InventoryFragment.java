package com.sellio.pos.ui.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.db.Inventory;
import com.sellio.pos.ui.activity.ProductDetailActivity;

import java.util.ArrayList;

/**
 * UI for Inventory, shows list of Product in the ProductCatalog.
 * Also use for a sale process of adding Product into sale.
 * 
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class InventoryFragment extends UpdatableFragment {
	protected static final int SEARCH_LIMIT = 0;

	InventoryListViewAdapter inventoryListViewAdapter = null;
	private ListView inventoryListView;
	private EditText searchKeyTextView;

	ArrayList<Inventory> inventories = new ArrayList<>();

	public InventoryFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.layout_inventory, container, false);

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
		String searchKey = searchKeyTextView.getText().toString();
		inventories = DatabaseManager.sharedInstance().inventoriesForBalance(searchKey);

		inventoryListViewAdapter.notifyDataSetChanged();
		inventoryListView.invalidateViews();
	}

	
	private void initUI(View view) {
		searchKeyTextView = view.findViewById(R.id.searchInventory);

		inventoryListViewAdapter = new InventoryListViewAdapter();
		inventoryListView = view.findViewById(R.id.inventoryListView);
		inventoryListView.setAdapter(inventoryListViewAdapter);

		searchKeyTextView.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				if (s.length() >= SEARCH_LIMIT) {
					update();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		});
	}

	private class InventoryListViewAdapter extends BaseAdapter {

		public InventoryListViewAdapter() {
			super();
		}

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
			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.listview_inventory, null);

			final Inventory inventory = (Inventory) getItem(position);

			Log.v("inventory", String.valueOf(inventory.getId()));

			TextView nameTextView = convertView.findViewById(R.id.nameTextView);
			nameTextView.setText(inventory.getProduct().getName());

			TextView balanceTextView = convertView.findViewById(R.id.balanceTextView);
			balanceTextView.setText(String.valueOf(inventory.getBalance()));

			Button modifyBtn = convertView.findViewById(R.id.btnModify);
			modifyBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ProductDetailActivity.product = inventory.getProduct();
					Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
					startActivity(intent);
				}
			});

			return convertView;
		}
	}
}