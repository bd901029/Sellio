package com.sellio.pos.ui.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class BaseFragmentActivity extends FragmentActivity {
	public void showErrorMessage(int messageID) {
		showErrorMessage(getResources().getString(messageID));
	}

	public void showErrorMessage(String sMsg) {
		new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage(sMsg)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	public void showMessage(String sTitle, String sMsg) {
		new AlertDialog.Builder(this)
				.setTitle(sTitle)
				.setMessage(sMsg)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	public void showToast(int messageID) {
		Toast.makeText(this, getResources().getString(messageID), Toast.LENGTH_SHORT).show();
	}

	public void showErrorToast(int messageID) {
		Toast.makeText(this, getResources().getString(messageID), Toast.LENGTH_LONG).show();
	}

	public void showErrorToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	ProgressDialog progressDialog = null;
	public void showProgress() {
		hideProgress();

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("One moment please...");
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	public void showProgress(String strMessage) {
		if (progressDialog != null) {
			progressDialog.setMessage(strMessage);
			return;
		}

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(strMessage);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
	}

	public void hideProgress() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public void hideSoftKeyboard() {
		if(getCurrentFocus()!=null) {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	public void showSoftKeyboard(View view) {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		view.requestFocus();
		inputMethodManager.showSoftInput(view, 0);
	}
}
