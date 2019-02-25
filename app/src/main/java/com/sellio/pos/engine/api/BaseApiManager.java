package com.sellio.pos.engine.api;

import android.content.Context;
import android.os.Handler;

import com.sellio.pos.SellioApplication;

public class BaseApiManager {

	public Callback callback = null;
	public interface Callback {
		void OnSuccess();
		void OnFailure(String message);
	}

	public void runOnSuccess() {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null)
					callback.OnSuccess();
			}
		});
	}

	public void runOnFailure(String message) {
		Context context = SellioApplication.sharedInstance();
		Handler handler = new Handler(context.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (callback != null)
					callback.OnFailure(message);
			}
		});
	}
}
