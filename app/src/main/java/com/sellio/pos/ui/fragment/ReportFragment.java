package com.sellio.pos.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sellio.pos.R;
import com.sellio.pos.engine.db.DatabaseManager;
import com.sellio.pos.engine.DateTimeStrategy;
import com.sellio.pos.engine.db.Sale;
import com.sellio.pos.engine.Utility;
import com.sellio.pos.ui.activity.SaleDetailActivity;

import java.util.ArrayList;
import java.util.Calendar;

import static com.sellio.pos.engine.Common.SortType.DAILY;
import static com.sellio.pos.engine.Common.SortType.MONTHLY;
import static com.sellio.pos.engine.Common.SortType.WEEKLY;
import static com.sellio.pos.engine.Common.SortType.YEARLY;

/**
 * UI for showing sale's record.
 * @author Refresh Team
 *
 */
public class ReportFragment extends UpdatableFragment {
	private Spinner sortSpinner;
	private TextView currentTimeTextView;
	private TextView profitTextView;
	private TextView totalTextView;

	ReportListViewAdapter reportListViewAdapter = new ReportListViewAdapter();
	private ListView reportListView;

	private Calendar currentTime = Calendar.getInstance(), startTime, endTime;

	ArrayList<Sale> sales = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View view = inflater.inflate(R.layout.layout_report, container, false);

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
		if (sortSpinner == null || currentTimeTextView == null || reportListView == null
				|| totalTextView == null || profitTextView == null)
			return;

		startTime = (Calendar) currentTime.clone();
		endTime = (Calendar) currentTime.clone();

		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);

		endTime.set(Calendar.HOUR_OF_DAY, 23);
		endTime.set(Calendar.MINUTE, 59);
		endTime.set(Calendar.SECOND, 59);

		int period = sortSpinner.getSelectedItemPosition();
		if (period == DAILY.getInt()) {
			currentTimeTextView.setText(" [" + DateTimeStrategy.getSQLDateFormat(currentTime) +  "] ");
			currentTimeTextView.setTextSize(16);
		} else if (period == WEEKLY.getInt()) {
			while(startTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
				startTime.add(Calendar.DATE, -1);
			}

			String toShow = " [" + DateTimeStrategy.getSQLDateFormat(startTime) +  "] ~ [";
			endTime = (Calendar) startTime.clone();
			endTime.add(Calendar.DATE, 7);
			toShow += DateTimeStrategy.getSQLDateFormat(endTime) +  "] ";
			currentTimeTextView.setTextSize(16);
			currentTimeTextView.setText(toShow);
		} else if (period == MONTHLY.getInt()){
			startTime.set(Calendar.DATE, 1);
			endTime = (Calendar) startTime.clone();

			endTime.add(Calendar.MONTH, 1);
			endTime.add(Calendar.DATE, -1);

			currentTimeTextView.setTextSize(18);
			currentTimeTextView.setText(" [" + currentTime.get(Calendar.YEAR) + "-" + (currentTime.get(Calendar.MONTH)+1) + "] ");
		} else if (period == YEARLY.getInt()){
			startTime.set(Calendar.DATE, 1);
			startTime.set(Calendar.MONTH, 0);

			endTime = (Calendar) startTime.clone();
			endTime.add(Calendar.YEAR, 1);
			endTime.add(Calendar.DATE, -1);
			currentTimeTextView.setTextSize(20);
			currentTimeTextView.setText(" [" + currentTime.get(Calendar.YEAR) +  "] ");
		}
		currentTime = startTime;

		sales = DatabaseManager.sharedInstance().reports(startTime.getTime(), endTime.getTime());

		reportListViewAdapter.notifyDataSetChanged();
		reportListView.invalidateViews();

		double totalPrice = 0, profit = 0;
		for (Sale sale : sales) {
			totalPrice += sale.getTotalPrice();
			profit += sale.getProfit();
		}
		totalTextView.setText(String.valueOf(totalPrice));
		profitTextView.setText(String.valueOf(profit));
	}

	void OnCurrentTimeClicked() {
		DatePickerDialog datePicker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int y, int m, int d) {
				currentTime.set(Calendar.YEAR, y);
				currentTime.set(Calendar.MONTH, m);
				currentTime.set(Calendar.DAY_OF_MONTH, d);
				update();
			}
		}, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
		datePicker.show();
	}

	private void initUI(View view) {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.period, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortSpinner = view.findViewById(R.id.spinnerSort);
		sortSpinner.setAdapter(adapter);
		sortSpinner.setSelection(2);
		sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				update();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
		currentTimeTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OnCurrentTimeClicked();
			}
		});

		Button previousButton = view.findViewById(R.id.btnPrevious);
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addDate(-1);
			}
		});

		Button nextButton = view.findViewById(R.id.btnNext);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				addDate(1);
			}
		});

		reportListView = view.findViewById(R.id.saleListView);
		totalTextView = view.findViewById(R.id.totalBox);
		profitTextView = view.findViewById(R.id.profitBox);

		reportListView.setAdapter(reportListViewAdapter);
		reportListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
				SaleDetailActivity.sale = sales.get(position);
				Intent intent = new Intent(getActivity(), SaleDetailActivity.class);
				getActivity().startActivity(intent);
			}
		});

	}

	private void addDate(int increment) {
		int period = sortSpinner.getSelectedItemPosition();
		if (period == DAILY.getInt()){
			currentTime.add(Calendar.DATE, 1 * increment);
		} else if (period == WEEKLY.getInt()){
			currentTime.add(Calendar.DATE, 7 * increment);
		} else if (period == MONTHLY.getInt()){
			currentTime.add(Calendar.MONTH, 1 * increment);
		} else if (period == YEARLY.getInt()){
			currentTime.add(Calendar.YEAR, 1 * increment);
		}

		update();
	}

	private class ReportListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return sales.size();
		}

		@Override
		public Object getItem(int position) {
			return sales.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.listview_report, null);
			}

			Sale sale = (Sale) getItem(position);

			TextView idTextView = convertView.findViewById(R.id.idTextView);
			idTextView.setText(String.valueOf(sale.getId()));

			TextView dateTextView = convertView.findViewById(R.id.dateTextView);
			dateTextView.setText(Utility.convertDateToText(sale.getDate()));

			TextView totalPriceTextView = convertView.findViewById(R.id.totalPriceTextView);
			totalPriceTextView.setText(String.valueOf(sale.getTotalPrice()));

			return convertView;
		}
	}
}
