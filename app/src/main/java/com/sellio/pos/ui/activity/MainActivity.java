package com.sellio.pos.ui.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sellio.pos.R;
import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.api.ApiManager;
import com.sellio.pos.engine.Utility;
import com.sellio.pos.ui.fragment.CartFragment;
import com.sellio.pos.ui.fragment.InventoryFragment;
import com.sellio.pos.ui.fragment.PosFragment;
import com.sellio.pos.ui.fragment.ProductFragment;
import com.sellio.pos.ui.fragment.ReportFragment;
import com.sellio.pos.ui.fragment.UpdatableFragment;

import java.util.Date;
import java.util.Locale;

/**
 * This UI loads 4 main pages (Product, Inventory, Pos, Report)
 * Makes the UI flow by slide through pages using ViewPager.
 *
 * @author Refresh Team
 *
 */
@SuppressLint("NewApi")
public class MainActivity extends BaseFragmentActivity {

	private static MainActivity instance = null;
	public static MainActivity sharedInstance() {
		return instance;
	}

	ProductFragment productFragment = null;
	InventoryFragment inventoryFragment = null;
	PosFragment posFragment = null;
	CartFragment cartFragment = null;
	ReportFragment reportFragment = null;

	private ViewPager viewPager;

	private PagerAdapter pagerAdapter;
	SharedPreferences pref;
	SharedPreferences.Editor pd;
	private static String KeyForData = "keyForData";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.layout_main);
		super.onCreate(savedInstanceState);

		instance = this;

		initiateUI();
	}

	@Override
	protected void onResume() {
		super.onResume();

		loadInformationAtFirstStartup();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.lang_en:
				setLanguage("en");
				return true;
			case R.id.lang_sw:
				setLanguage("sw");
				return true;
			case R.id.about:
				showAbout();
				return true;
			case R.id.license:
				showSubscription();
				return true;
			case R.id.activate:
				showActivation();
				return true;

			case R.id.btnSignOut:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			openQuitDialog();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initiateUI() {
		viewPager = findViewById(R.id.pager);

		initiateActionBar();

		FragmentManager fragmentManager = getSupportFragmentManager();
		pref = getSharedPreferences(KeyForData, Context.MODE_PRIVATE);
		pd = pref.edit();
		pagerAdapter = new PagerAdapter(fragmentManager, getResources());
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (Utility.SDK_SUPPORTED)
					getActionBar().setSelectedNavigationItem(position);
			}
		});
		viewPager.setCurrentItem(2);
	}

	private void initiateActionBar() {
		if (Utility.SDK_SUPPORTED) {
			ActionBar actionBar = getActionBar();

			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

			ActionBar.TabListener tabListener = new ActionBar.TabListener() {
				@Override
				public void onTabReselected(Tab tab, FragmentTransaction ft) {
				}

				@Override
				public void onTabSelected(Tab tab, FragmentTransaction ft) {
					viewPager.setCurrentItem(tab.getPosition()) ;
//					View tabLayout = (View) tab.getCustomView();
//					tabLayout.setBackgroundResource(R.drawable.tabs_indicator);
//					tab.setCustomView(tabLayout);
				}

				@Override
				public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				}
			};

			actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.product).toUpperCase()).setTabListener(tabListener), 0, false);
			actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.inventory).toUpperCase()).setTabListener(tabListener), 1, false);
			actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.pos).toUpperCase()).setTabListener(tabListener), 2, true);
			actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.cart).toUpperCase()).setTabListener(tabListener), 3, false);
			actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.report).toUpperCase()).setTabListener(tabListener), 4, false);
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#009999")));
		}
	}

	public void update() {
		if (productFragment != null) {
			productFragment.update();
		}

		if (inventoryFragment != null) {
			inventoryFragment.update();
		}

		if (posFragment != null) {
			posFragment.update();
		}

		if (productFragment != null) {
			productFragment.update();
		}

		if (cartFragment != null) {
			cartFragment.update();
		}

		if (reportFragment != null) {
			reportFragment.update();
		}
	}

	void loadInformationAtFirstStartup() {
		if (!SettingManager.sharedInstance().isFirstRun()) {
			ApiManager.sharedInstance().start();
			return;
		}

		showProgress();
		ApiManager.sharedInstance().downloadAllExistingInformation(new ApiManager.Callback() {
			@Override
			public void OnSuccess() {
				hideProgress();

				update();

				ApiManager.sharedInstance().start();
			}

			@Override
			public void OnFailure(String message) {
				hideProgress();
			}
		});
	}

	/**
	 * Open quit dialog.
	 */
	private void openQuitDialog() {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(
				MainActivity.this);
		quitDialog.setTitle(getResources().getString(R.string.dialog_quit));
		quitDialog.setPositiveButton(getResources().getString(R.string.quit), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		quitDialog.setNegativeButton(getResources().getString(R.string.no), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		quitDialog.show();
	}

	public ViewPager getViewPager() {
		return viewPager;
	}

	private void showSubscription() {
//		startActivity( new Intent(MainActivity.this, SubscriptionActivity.class));
	}

	private void showAbout() {
		startActivity( new Intent(MainActivity.this, AboutActivity.class));
	}

	// private void OnAddProductBtnClicked() {
	//     AboutFragment newFragment = new AboutFragment();
	//  newFragment.show( getSupportFragmentManager(), "");
	//}
	private void showActivation() {
		new AlertDialog.Builder(this)
				.setTitle("Activate Your Sellio")
				.setMessage("Do you Wish To activate your Sellio App. This will end your trial period  and begin your official usage period. You will be entitled full customer support and software updated")
				.setCancelable(false)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						long OneSweetDay  = 1000;

						long	expiry_date=new Date().getTime()-OneSweetDay;
						pd.putLong("expiry_date", expiry_date);
						pd.commit();
						startActivity( new Intent(MainActivity.this, SplashScreenActivity.class));

					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//re-request
						dialog.cancel();
					}
				})
				.setIcon(R.drawable.success_bow)
				.show();

	}

	private void setLanguage(String localeString) {
		Locale locale = new Locale(localeString);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	class PagerAdapter extends FragmentStatePagerAdapter {

		private UpdatableFragment[] fragments;
		private String[] fragmentNames;

		/**
		 * Construct a new PagerAdapter.
		 * @param fragmentManager
		 * @param res
		 */
		public PagerAdapter(FragmentManager fragmentManager, Resources res) {
			super(fragmentManager);

			reportFragment = new ReportFragment();
			cartFragment = new CartFragment(reportFragment);
			posFragment = new PosFragment(cartFragment);
			inventoryFragment = new InventoryFragment();
			productFragment = new ProductFragment();

			fragments = new UpdatableFragment[] {productFragment, inventoryFragment, posFragment, cartFragment, reportFragment};
			fragmentNames = new String[] {res.getString(R.string.product), res.getString(R.string.inventory), res.getString(R.string.pos), res.getString(R.string.cart), res.getString(R.string.report)};
		}

		@Override
		public Fragment getItem(int i) {
			return fragments[i];
		}

		@Override
		public int getCount() {
			return fragments.length;
		}

		@Override
		public CharSequence getPageTitle(int i) {
			return fragmentNames[i];
		}

		/**
		 * Update
		 * @param index
		 */
		public void update(int index) {
			fragments[index].update();
		}
	}
}