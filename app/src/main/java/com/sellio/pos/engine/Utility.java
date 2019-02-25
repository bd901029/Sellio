package com.sellio.pos.engine;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.sellio.pos.R;
import com.sellio.pos.SellioApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {
	private static Utility instance = null;
	public static Utility SharedInstance() {
		if (instance == null)
			instance = new Utility();
		return instance;
	}

	Utility() {
	}

	public static boolean SDK_SUPPORTED = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

	public static int RESULT_LOAD_IMAGE = 0x1029;

	public static boolean isDataConnectionAvailable() {
		Context context = SellioApplication.sharedInstance();
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null)
			return false;
		return connectivityManager.getActiveNetworkInfo().isConnected();
	}

	public static void setLanguage(String localeString) {
		Locale locale = new Locale(localeString);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;

		Context context = SellioApplication.sharedInstance();
		context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
	}

	public static byte[] convertDrawableToData(BitmapDrawable drawable) {
		if (drawable == null)
			return null;

		try {
			Bitmap bitmap = drawable.getBitmap();
			Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmapResized.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static Bitmap convertByteToBitmap(byte[] bytes) {
		if (bytes == null || bytes.length <= 0)
			return null;

		Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return bitmap;
	}

	public static String convertDateToText(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	public static Date convertTextToDate(String strDate) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return dateFormat.parse(strDate);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void requestStoragePermission(Activity activity) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			// Provide an additional rationale to the user if the permission was not granted
			// and the user would benefit from additional context for the use of the permission.
			// For example if the user has previously denied the permission.
			new AlertDialog.Builder(activity)
					.setTitle("Permission Request")
					.setMessage("Requesting Permission to Access Internet")
					.setCancelable(false)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//re-request
							ActivityCompat.requestPermissions(activity,
									new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
									0);
						}
					})
					.setIcon(R.drawable.warning_sigh)
					.show();
		} else {
			// READ_PHONE_STATE permission has not been granted yet. Request it directly.
			ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
		}
	}

	public static void writeByteToFile(byte[] bytes, String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(bytes);
			bos.flush();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap bitmapFromFile(String picturePath) {
		if (picturePath == null || picturePath.equals(""))
			return null;

		return BitmapFactory.decodeFile(picturePath);
	}

	public static String getUniqueIMEIId() {
		Context context = SellioApplication.sharedInstance();
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				return "";
			}

			String imei = telephonyManager.getDeviceId();
			if (imei != null && !imei.isEmpty()) {
				return imei;
			} else {
				return android.os.Build.SERIAL;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "not_found";
	}
}
