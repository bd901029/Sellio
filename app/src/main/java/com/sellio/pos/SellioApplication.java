package com.sellio.pos;

import android.app.Application;

import com.sellio.pos.engine.api.ApiManager;

public class SellioApplication extends Application {
	static SellioApplication sharedInstance = null;
	public static SellioApplication sharedInstance() {
		return sharedInstance;
	}

	@Override
	public void onCreate() {
		sharedInstance = this;

		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		ApiManager.sharedInstance().stop();
	}
}
