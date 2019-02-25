package com.sellio.pos.engine;

import android.content.Context;
import android.content.SharedPreferences;

import com.sellio.pos.SellioApplication;
import com.sellio.pos.engine.api.User;

import static android.content.Context.MODE_PRIVATE;

public class SettingManager {
	private static SettingManager instance = null;
	public static SettingManager sharedInstance() {
		if (instance == null)
			instance = new SettingManager();
		return instance;
	}

	SharedPreferences preferences = null;

	SettingManager() {
		Context context = SellioApplication.sharedInstance();
		preferences = context.getSharedPreferences(context.getPackageName(), MODE_PRIVATE);
	}

	public boolean isFirstRun() {
		if (preferences.getBoolean("firstrun", true)) {
			preferences.edit().putBoolean("firstrun", false).commit();
			return true;
		}

		return false;
	}

	public void loadUser() {
		User.sharedInstance().username = preferences.getString("username", "");
		User.sharedInstance().password = preferences.getString("password", "");
		User.sharedInstance().roleType = User.RoleType.fromInt(preferences.getInt("role", User.RoleType.None.getInt()));
	}

	public void saveUser() {
		preferences.edit().putString("username", User.sharedInstance().username).commit();
		preferences.edit().putString("password", User.sharedInstance().password).commit();
		preferences.edit().putInt("role", User.sharedInstance().roleType.getInt()).commit();
	}
}
