package com.sellio.pos.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.sellio.pos.R;
import com.sellio.pos.engine.api.ApiManager;
import com.sellio.pos.engine.api.User;

public class LoginActivity extends BaseActivity {

	EditText usernameEditText, passwordEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		initUI();

		autoLogin();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			openQuitDialog();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void OnLogInBtnClicked(View view) {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();

		login(username, password);
	}

	public void OnSignUpBtnClicked(View view) {
		Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
		startActivity(intent);
	}

	void initUI() {
		usernameEditText = findViewById(R.id.usernameEditText);
		passwordEditText = findViewById(R.id.passwordEditText);

		if (User.sharedInstance().hasLoggedIn()) {
			usernameEditText.setText(User.sharedInstance().username);
			passwordEditText.setText(User.sharedInstance().password);
		}
	}

	void autoLogin() {
		if (!User.sharedInstance().hasLoggedIn())
			return;

		login(User.sharedInstance().username, User.sharedInstance().password);
	}

	void login(String username, String password) {
		if (username.equals("") && password.equals("")) {
			showErrorToast(R.string.please_input_all);
			return;
		}

		showProgress();
		User.sharedInstance().login(username, password, new ApiManager.Callback() {
			@Override
			public void OnSuccess() {
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);

				hideProgress();
			}

			@Override
			public void OnFailure(String message) {
				hideProgress();
				showErrorToast(message);
			}
		});
	}

	private void openQuitDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
		dialog.setTitle(getResources().getString(R.string.dialog_quit));
		dialog.setPositiveButton(getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});

		dialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		dialog.show();
	}
}
