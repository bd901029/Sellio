package com.sellio.pos.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.api.User;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.db.Product;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.activity.ProductDetailActivity;
import com.sellio.pos.ui.dialog.AddProductDialogFragment;

import java.util.ArrayList;

/**
 * Created by ivan on 5/10/2018.
 */

public class ProductFragment extends UpdatableFragment {

    Button addProductButton = null;
    ProductListViewAdapter productListViewAdapter = new ProductListViewAdapter();
    private ListView productListView = null;
    private Spinner categorySpinner = null;

    ArrayList<Product> products = new ArrayList<>();
    ArrayList<String> categories = new ArrayList<>();

    public ProductFragment() {
        super();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.layout_product, container, false);

		initiateUI(view);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		update();
	}

	private void initiateUI(View view) {
		categorySpinner = view.findViewById(R.id.spinner_catalog);
		categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				updateProductList();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


		productListView = view.findViewById(R.id.productListView);
		productListView.setAdapter(productListViewAdapter);

		addProductButton = view.findViewById(R.id.addProductButton);
		addProductButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				OnAddProductBtnClicked();
			}
		});
	}

    @Override
    public void update() {
    	if (categorySpinner == null)
    		return;

		int curCategoryIndex = categorySpinner.getSelectedItemPosition();
		categories = DatabaseManager.sharedInstance().categories();
		categories.add(0, "ALL");

		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(spinnerAdapter);
		categorySpinner.setSelection((curCategoryIndex >= categories.size()) ? 0 : curCategoryIndex);

		updateProductList();
    }

    public void updateProductList() {
    	if (categorySpinner == null)
    		return;

		int curCategoryIndex = categorySpinner.getSelectedItemPosition();
		if (curCategoryIndex <= 0) {
			products = DatabaseManager.sharedInstance().allProducts();
		} else {
			String category = categories.get(curCategoryIndex);
			products = DatabaseManager.sharedInstance().productsByCategory(category);
		}

		productListViewAdapter.notifyDataSetChanged();
		productListView.invalidateViews();
	}

    public void OnAddProductBtnClicked() {
    	if (!User.sharedInstance().isAdmin())
    		return;

        AddProductDialogFragment newFragment = new AddProductDialogFragment(ProductFragment.this);
        newFragment.show(getFragmentManager(), "");
    }

    private class ProductListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return products.size();
        }

        @Override
        public Object getItem(int position) {
            return products.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(ProductFragment.this.getActivity()).inflate(R.layout.listview_product, null);

			final Product product = (Product) getItem(position);

			Log.v("product", String.valueOf(product.getId()));

			TextView nameTextView = convertView.findViewById(R.id.nameTextView);
			nameTextView.setText(product.getName());

			TextView unitTextView = convertView.findViewById(R.id.unit);
			unitTextView.setText(product.getUnit());

			TextView costTextView = convertView.findViewById(R.id.cost);
			costTextView.setText(String.valueOf(product.getCostPrice()));

			TextView saleTextView = convertView.findViewById(R.id.sale);
			saleTextView.setText(String.valueOf(product.getSalePrice()));

			Button modifyBtn = convertView.findViewById(R.id.btnModify);
			modifyBtn.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					ProductDetailActivity.product = product;
					Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
					startActivity(intent);
				}
			});

			Button btnDelete = convertView.findViewById(R.id.btnDelete);
			btnDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!User.sharedInstance().isAdmin())
						return;

					String strMessage = String.format("Are you sure you want to delete named \"%s\"?", product.getName());
					new AlertDialog.Builder(getActivity())
							.setTitle(null)
							.setMessage(strMessage)
							.setPositiveButton(getResources().getText(R.string.delete), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									DatabaseManager.sharedInstance().deleteProduct(product, new DatabaseManager.Callback() {
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
							})
							.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							})
							.show();
				}
			});

			return convertView;
        }
    }
}
