package com.sellio.pos.engine.api;

import android.os.AsyncTask;

import com.sellio.pos.engine.SettingManager;
import com.sellio.pos.engine.Utility;

import org.json.JSONObject;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

public class User extends BaseApiManager {
	private static User instance = null;
	public static User sharedInstance() {
		if (instance == null) {
			instance = new User();
			SettingManager.sharedInstance().loadUser();
		}
		return instance;
	}

	public enum RoleType {
		None(-1),
		Admin(0),
		Cashier(1);

		private int intValue;
		RoleType(int value) {
			intValue = value;
		}

		@Override
		public String toString() {
			switch (intValue) {
				case 0:
					return "0";
				case 1:
					return "1";
				default:
					return "-1";
			}
		}

		public int getInt() {
			return intValue;
		}

		public static RoleType fromInt(int value) {
			switch (value) {
				case 0:
					return Admin;
				case 1:
					return Cashier;
				default:
					return None;
			}
		}
	}

	public String username = "";
	public String password = "";
	public RoleType roleType = RoleType.None;

	User() {
		super();
	}

	private void fromJSON(JSONObject info) {
		try {
			username = info.getString("username");
			password = info.getString("password");
			roleType = RoleType.fromInt(info.getInt("role"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hasLoggedIn() {
		return (!username.equals("") && !password.equals(""));
	}

	public void signup(String username, String password, RoleType roleType, ApiManager.Callback callback) {
		this.callback = callback;

		new SignupAsyncTask().execute(username, password, roleType.toString());
	}

	public void login(String username, String password, ApiManager.Callback callback) {
		this.callback = callback;

		new LoginAsyncTask().execute(username, password);
	}

	public boolean isAdmin() {
		return (roleType == RoleType.Admin);
	}

	private class SignupAsyncTask extends AsyncTask {
		@Override
		protected void onPreExecute() {}

		@Override
		protected String doInBackground(Object[] params) {
			String errorMsg = null;
			try {
				String strUrl = "http://sellio.posmart.co.ke/api/signup.php";

				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.addTextBody("username", (String) params[0]);
				entityBuilder.addTextBody("password", (String) params[1]);
				entityBuilder.addTextBody("role", (String) params[2]);


				HttpEntity httpEntity = entityBuilder.build();
				HttpPost request = new HttpPost(strUrl);
				request.setEntity(httpEntity);
				HttpResponse resp = new DefaultHttpClient().execute(request);
				String strResponse = EntityUtils.toString(resp.getEntity()).trim();
				JSONObject jsonObject = new JSONObject(strResponse);

				boolean isSucceed = jsonObject.getBoolean("success");
				if (isSucceed) {
					JSONObject info = jsonObject.getJSONObject("info");
					fromJSON(info);
					SettingManager.sharedInstance().saveUser();
				} else {
					errorMsg = jsonObject.getString("info");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return errorMsg;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (result == null) {
				runOnSuccess();
			} else {
				runOnFailure((String)result);
			}
		}
	}

	private class LoginAsyncTask extends AsyncTask {
		@Override
		protected void onPreExecute() {}

		@Override
		protected String doInBackground(Object[] params) {
			String errorMsg = null;
			String imei = Utility.getUniqueIMEIId();
			try {
				String strUrl = "http://sellio.posmart.co.ke/api/login.php";

				MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
				entityBuilder.addTextBody("username", (String) params[0]);
				entityBuilder.addTextBody("password", (String) params[1]);

				HttpEntity httpEntity = entityBuilder.build();
				HttpPost request = new HttpPost(strUrl);
				request.setEntity(httpEntity);
				HttpResponse resp = new DefaultHttpClient().execute(request);
				String strResponse = EntityUtils.toString(resp.getEntity()).trim();
				JSONObject jsonObject = new JSONObject(strResponse);

				boolean isSucceed = jsonObject.getBoolean("success");
				if (isSucceed) {
					JSONObject info = jsonObject.getJSONObject("info");
					fromJSON(info);
					SettingManager.sharedInstance().saveUser();
				} else {
					errorMsg = jsonObject.getString("info");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return errorMsg;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (result == null) {
				runOnSuccess();
			} else {
				runOnFailure((String)result);
			}
		}
	}
}
