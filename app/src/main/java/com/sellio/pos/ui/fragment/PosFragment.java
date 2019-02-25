package com.sellio.pos.ui.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.api.User;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.db.Product;
import com.sellio.pos.engine.Utility;
import com.sellio.pos.ui.activity.BaseFragmentActivity;
import com.sellio.pos.ui.activity.MainActivity;

import static java.lang.Math.abs;

/**
 * UI for Sale operation.
 * @author Refresh Team
 *
 */
@SuppressLint("ValidFragment")
public class PosFragment extends UpdatableFragment {

	CartFragment cartFragment = null;

	private TextView searchEditText;
	private ImageButton btnReset;
	ArrayList<Button> categoryButtons = new ArrayList<>();

	LinearLayout categoryButtonContainer;
	ProductGridViewAdapter productGridViewAdapter = new ProductGridViewAdapter();
	GridView productGridView;

	String selectedCategory = null;

	private ArrayList<Product> products = new ArrayList<>();
	private ArrayList<String> categories = new ArrayList<>();

	public PosFragment(CartFragment cartFragment) {
		super();

		this.cartFragment = cartFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.layout_pos, container, false);

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
		String key = searchEditText.getText().toString();
		products = DatabaseManager.sharedInstance().productsByKey(key);
		categories = DatabaseManager.sharedInstance().categories();

		productGridViewAdapter.notifyDataSetChanged();
		productGridView.invalidateViews();

		categoryButtonContainer.removeAllViews();
		categoryButtons.clear();
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
		params.setMargins(0, 0, 30, 0);
		for (String category : categories) {
			Button btnCategory = new Button(getActivity(), null, R.style.ButtonText );
			btnCategory.setWidth(300);
			btnCategory.setHeight(100);
			btnCategory.setPadding(0,0, 0,0);
			btnCategory.setGravity(Gravity.CENTER);

			btnCategory.setLayoutParams(params);
			btnCategory.setTextColor(Color.BLACK);
			btnCategory.setTextSize(12);
			btnCategory.setBackgroundResource(R.drawable.btn_lightgreen);
			btnCategory.setText(category);
			btnCategory.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int index = categoryButtonContainer.indexOfChild(v);
					selectedCategory = categories.get(index);

					updateProductGrid();
				}
			});

			if (selectedCategory == null || !selectedCategory.equals(category)) {
				btnCategory.setBackgroundResource(R.drawable.btn_lightgreen);
			} else {
				btnCategory.setBackgroundResource(R.drawable.btn_yellow);
			}

			categoryButtonContainer.addView(btnCategory);
			categoryButtons.add(btnCategory);
		}
	}

	void OnResetBtnClicked() {
		selectedCategory = null;
		searchEditText.setText("");

		update();
	}

	private void initUI(View view) {
		productGridView = view.findViewById(R.id.productGridView);
		productGridViewAdapter = new ProductGridViewAdapter();
		productGridView.setAdapter(productGridViewAdapter);
		productGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Product product = (Product) productGridViewAdapter.getItem(position);
				DatabaseManager.sharedInstance().addCart(product, 1, new DatabaseManager.Callback() {
					@Override
					public void OnSuccess() {
						ViewPager viewPager = ((MainActivity)getActivity()).getViewPager();
						viewPager.setCurrentItem(3);

						cartFragment.update();
					}

					@Override
					public void OnFailure(String message) {
						((BaseFragmentActivity)getActivity()).showErrorMessage(message);
					}
				});
			}
		});

		categoryButtonContainer = view.findViewById(R.id.categoryButtonContainer);

		btnReset = view.findViewById(R.id.btnReset);

		btnReset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnResetBtnClicked();
			}
		});

		searchEditText = view.findViewById(R.id.searchEditText);
		searchEditText.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				selectedCategory = null;

				updateProductGrid();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		});
	}

	void updateProductGrid() {
		if (selectedCategory == null) {
			String key = searchEditText.getText().toString();
			products = DatabaseManager.sharedInstance().productsByKey(key);
		} else {
			products = DatabaseManager.sharedInstance().productsByCategory(selectedCategory);
		}

		productGridViewAdapter.notifyDataSetChanged();
		productGridView.invalidateViews();

		for (int i = 0; i < categories.size(); i++) {
			String category = categories.get(i);
			if (selectedCategory == null || !selectedCategory.equals(category)) {
				categoryButtons.get(i).setBackgroundResource(R.drawable.btn_lightgreen);
			} else {
				categoryButtons.get(i).setBackgroundResource(R.drawable.btn_yellow);
			}
		}
	}

	public class ProductGridViewAdapter extends BaseAdapter {
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
			return products.get(position).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(PosFragment.this.getActivity()).inflate(R.layout.listview_productitem, null);

			Product product = (Product) getItem(position);

			ImageView imageView = convertView.findViewById(R.id.imageView);
			Bitmap bitmap = product.getPhotoBitmap();
			if (bitmap == null)
				imageView.setBackgroundColor(Color.argb(255, 229, 229, 229));
			else
				imageView.setImageBitmap(bitmap);

			TextView priceTextView = convertView.findViewById(R.id.unitPriceTextView);
			priceTextView.setText(String.valueOf(product.getSalePrice()));

			TextView nameTextView = convertView.findViewById(R.id.nameTextView);
			nameTextView.setText(product.getName());

			return convertView;
		}
	}
}
