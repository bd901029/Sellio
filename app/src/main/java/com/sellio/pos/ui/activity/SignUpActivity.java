package com.sellio.pos.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.sellio.pos.R;
import com.sellio.pos.engine.api.ApiManager;
import com.sellio.pos.engine.api.User;

public class SignUpActivity extends BaseActivity {

	EditText usernameEditText, passwordEditText, confirmEditText;
	CheckBox masterCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		initUI();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	void initUI() {
		usernameEditText = findViewById(R.id.usernameEditText);
		passwordEditText = findViewById(R.id.passwordEditText);
		confirmEditText = findViewById(R.id.confirmEditText);
		masterCheckBox = findViewById(R.id.checkBoxIsMaster);
	}

	public void OnSignUpBtnClicked(View view) {
		String username = usernameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		String confirm = confirmEditText.getText().toString();
		User.RoleType roleType = masterCheckBox.isChecked() ? User.RoleType.Admin : User.RoleType.Cashier;

		if (username.equals("") || password.equals("") || confirm.equals("")) {
			showErrorToast(R.string.please_input_all);
			return;
		}

		if (!password.equals(confirm)) {
			showErrorMessage("Password isn't matched.");
			return;
		}

		showProgress();
		User.sharedInstance().signup(username, password, roleType, new ApiManager.Callback() {
			@Override
			public void OnSuccess() {
				hideProgress();

				Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
				startActivity(intent);

				finish();
			}

			@Override
			public void OnFailure(String message) {
				hideProgress();

				showErrorMessage(message);
			}
		});
	}
}
