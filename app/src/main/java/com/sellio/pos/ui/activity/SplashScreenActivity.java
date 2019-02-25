package com.sellio.pos.ui.activity;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import android.app.AlertDialog;
import  android.content.pm.PackageManager;
import java.io.StringWriter;
import java.io.Writer;
import android.Manifest;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;
import com.sellio.pos.R;
import android.support.v4.app.ActivityCompat;
import com.sellio.pos.engine.DateTimeStrategy;
import com.sellio.pos.engine.Utility;

import android.telephony.TelephonyManager;

import com.loopj.android.http.*;
import com.sellio.pos.engine.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;

import cz.msebera.android.httpclient.Header;

/**
 * This is the first activity page, core-app and database created here.
 * Dependency injection happens here.
 *
 * @author Refresh Team
 *
 */
public class SplashScreenActivity extends Activity {

	private static final long SPLASH_TIMEOUT = 1000;
	private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
	private static final int MY_PERMISSIONS_REQUEST_INTERNET= 0;

    public static final String POS_VERSION = "Mobile POS 0.8";

	public static final String KEY_IMEI = "IMEINumber";

	private LinearLayout activation_form;

    private Button activateBtn = null,
			launchBtn = null,
			purchaseVoucherBtn = null,
			contactUsBtn = null;

	private EditText phoneEditText;
	private EditText shopNameEditText;
	private EditText voucherEditText;
	private TextView infoTextView;
	private TextView imeiTextView;
	private ProgressBar spinner;

    private boolean gone;
	public boolean success;

    private TelephonyManager telephonyManager;

    private Date newExpiryDate;
    private String new_expiry_date;
    private String strVoucherNo;

    SharedPreferences preferences = null;
    SharedPreferences.Editor preferencesEditor = null;

    String BASE_URL = "http://sellio.posmart.co.ke/activate/"; //http://ur link to ur API

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static String KeyForData = "keyForData";

    long install_date;
    long expiry_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splashscreen);

        initiateUI();
        initiateCoreApp();


      /*  if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();


        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            requestInternetPermission();

        }

        */

		preferences = getSharedPreferences(KeyForData, Context.MODE_PRIVATE);
		preferencesEditor = preferences.edit();
        if( preferences.contains("shop") ) {
            shopNameEditText.setText( "" + preferences.getString("shop", "") );
        }

        if( preferences.contains("phoneEditText") ) {
            phoneEditText.setText( "" + preferences.getString("phoneEditText", "") );
        }

        if( preferences.contains("install_date") ) {
            install_date = preferences.getLong("install_date", 1);
            expiry_date = preferences.getLong("expiry_date", 1);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            System.out.println("Expires"+dateFormat.format(expiry_date));
            System.out.println("Installed"+dateFormat.format(install_date));
        }
        else
    		{
            //Constants.sidkey = "";
            // long timestamp=
            install_date = new Date().getTime();
            long expiryInMillies = TimeUnit.DAYS.toMillis(30);

            expiry_date=install_date + expiryInMillies;
            preferencesEditor.putLong("expiry_date", expiry_date);
            preferencesEditor.putLong("install_date", install_date);
            preferencesEditor.putBoolean("activate", false);
            preferencesEditor.putString("shop", shopNameEditText.getText().toString());
            preferencesEditor.putString("phoneEditText", phoneEditText.getText().toString());

            preferencesEditor.commit();
        }
    }

	public  void OnActivateBtnClicked(View view) {
		String s = voucherEditText.getText().toString();
		if (s.equals("")) {
			infoTextView.setText("Voucher Code is required to activate");
			return;
		}

//*2017-07-02#$3!!!0@ndr01d@pp#  28char
		int  c = countOccurrences(s,'#',0 );
		System.out.println("#"+c);
		if (c == 2) {
			ActivateOffline(s);
			return;
		}

		boolean connected = Utility.isDataConnectionAvailable();
		System.out.println("Connected:" + ""+connected+"");
		if (!connected) {
			infoTextView.setText("Please connect to the internet to activate");
		} else {
			activateBtn.setEnabled(false);
			infoTextView.setText("Please wait as we activate your Sellio App");
			if (RenewOrRegisterDevice()) {
				launchBtn.setVisibility(View.VISIBLE);
			}
		}
	}

	public void OnPurchaseVoucherBtnClicked(View view) {
		boolean  connected = Utility.isDataConnectionAvailable();
		if (connected) {
//			startActivity(new Intent(SplashScreenActivity.this, SubscriptionActivity.class));
		} else {
			infoTextView.setText("Please Connect to the internet to Activate");
		}
	}

	public void OnContactUsBtnClicked(View view) {
		Intent intent = new Intent(SplashScreenActivity.this, AboutActivity.class);
		startActivity(intent);
	}

	public void OnLaunchBtnClicked(View view) {
		Intent newActivity = new Intent(SplashScreenActivity.this, LoginActivity.class);
		startActivity(newActivity);
		SplashScreenActivity.this.finish();
	}

    /**
     * Loads database and DAO.
     */
    private void initiateCoreApp() {
        DateTimeStrategy.setLocale("th", "TH");
        Log.d("Core App", "INITIATE");
    }

    private static int countOccurrences(String someString, char searchedChar, int index) {
        if (index >= someString.length()) {
            return 0;
        }

        int count = someString.charAt(index) == searchedChar ? 1 : 0;
        return count + countOccurrences(someString, searchedChar, index + 1);
    }
    /**
     * Go.
     */
    private void go() {
        checkExpiry();
        gone = true;
    }

    private  void ActivateOffline(String s){
        //*30#$31110@ndr01d@pp#  28char

        String pwd = s.substring(s.indexOf("#") + 1, s.lastIndexOf("#"));
        String ndays = s.substring(s.indexOf("*") + 1, s.indexOf("#"));
        System.out.println("pwd"+pwd);
        System.out.println("Date"+ndays);
        if(isInteger(ndays) && pwd.equals("$3!!!0@ndr01d@pp")) {
            long extend = TimeUnit.DAYS.toMillis(Integer.parseInt(ndays));
            expiry_date=new Date().getTime() +extend;
            System.out.println("Date"+expiry_date);
            preferencesEditor.putLong("expiry_date", expiry_date);
            preferencesEditor.putString("voucherEditText", "SuperSellio");

            preferencesEditor.commit();

            SplashScreenActivity.this.finish();

            Intent newActivity = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(newActivity);
        }
    }

    public boolean isInteger(String string) {
        boolean parsable = true;
        try{
            Integer.parseInt(string);
        }catch(NumberFormatException e){
            parsable = false;
        }
        return parsable;
    }

    private void initiateUI() {
		infoTextView = findViewById(R.id.info);
		imeiTextView = findViewById(R.id.imei);
		phoneEditText = findViewById(R.id.phonenumber);
		shopNameEditText = findViewById(R.id.nameTextView);
		spinner = findViewById(R.id.progressBar);
		voucherEditText = findViewById(R.id.voucher);

		activation_form = findViewById(R.id.registration);

		activateBtn = findViewById(R.id.btnActivate);
		launchBtn = findViewById(R.id.launch);
		purchaseVoucherBtn = findViewById(R.id.btnPurchaseVoucher);
		contactUsBtn = findViewById(R.id.btnContactUs);

        activation_form = findViewById(R.id.registration);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gone)
                	go();
            }
        }, SPLASH_TIMEOUT);
    }

    private void checkExpiry() {
        //     long  expiryInMilliesTest = TimeUnit.DAYS.toMillis(30);
        // expiry_date=base_date - expiryInMilliesTest;
        expiry_date = preferences.getLong("expiry_date", 1);
        long timestamp=new Date().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("CExpires" + expiry_date);
        System.out.println("CInstalled" + install_date);
        System.out.println("leo" + timestamp);

        if ( timestamp > expiry_date ) {
            infoTextView.setText("Hello, your trial expired on " + dateFormat.format(expiry_date) );
            activation_form.setVisibility(View.VISIBLE);
        }
        else
        	{
            Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();
        }
    }
    private  void  restart(){
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
    private boolean RenewOrRegisterDevice() {
        success = false;

        String Imei = phoneEditText.getText().toString();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
            restart();


        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            requestInternetPermission(); restart();

        }

        telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        Imei = telephonyManager.getDeviceId();


        imeiTextView.setText("Phone Imei: " + Imei);



        RequestParams rp = new RequestParams();
        rp.add("imei", Imei);
        rp.add("phoneEditText", phoneEditText.getText().toString());
        rp.add("voucherEditText", voucherEditText.getText().toString());
        rp.add("shop", shopNameEditText.getText().toString());

        spinner.setVisibility(View.VISIBLE);
        HttpUtils.get(BASE_URL, rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("asd", "---------------- this is response : " + response);

                activateBtn.setEnabled(true);
                try {
                    new_expiry_date = response.getJSONObject(0).getString("expiry_date");
                    strVoucherNo = response.getJSONObject(0).getString("active_voucher");
                    System.out.println("object " + new_expiry_date);
                    SimpleDateFormat sdf = formatter;
                    try {
                        newExpiryDate= sdf.parse(new_expiry_date);
                        preferencesEditor.putLong("expiry_date", newExpiryDate.getTime());
                        preferencesEditor.putString("voucherEditText", strVoucherNo);
                        preferencesEditor.putBoolean("activated", true);
                        preferencesEditor.putString("shop", shopNameEditText.getText().toString() );
                        preferencesEditor.putString("phoneEditText", phoneEditText.getText().toString() );
                        preferencesEditor.commit();
                        spinner.setVisibility(View.INVISIBLE);

                    } catch (ParseException ex) {
                        success = false;
						activateBtn.setEnabled(true);
                        infoTextView.setText("Activation Failed. Please try again Later");
                        ex.printStackTrace();
                        spinner.setVisibility(View.INVISIBLE);


                    }


                } catch (JSONException e) {
                    infoTextView.setText("Activation Failed. Please Try again Later");
                    e.printStackTrace();
					activateBtn.setEnabled(true);
                    spinner.setVisibility(View.INVISIBLE);
                    success = false;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                infoTextView.setText("Activation Failed. Please Try again Later"+ responseString );
                System.out.println(responseString);
				activateBtn.setEnabled(true);
                spinner.setVisibility(View.INVISIBLE);
                success = false;
                RenewOrRegisterDevice();
            }
        });

//             expiry_date = newExpiryDate.getTime();
        preferences = getSharedPreferences(KeyForData, Context.MODE_PRIVATE);
        long timestamp = new Date().getTime();
        expiry_date= preferences.getLong("expiry_date",1);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("NEw  Expiry St as" + dateFormat.format(expiry_date));
        System.out.println("timestap is  " + dateFormat.format(timestamp));
        if (expiry_date >= timestamp) {
            infoTextView.setText("Activation Successful!  Valid until  " + dateFormat.format(expiry_date) + ".  Tap the launchBtn button to continue. Happy Selling");


            success = true;
        }
        else {
            infoTextView.setText("Current License Expires:  " + dateFormat.format(expiry_date));

        }

        return success;
    }


    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(this)
                    .setTitle("Permission Request")
                    .setMessage("Requesting Permission to Read your Phone State")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(SplashScreenActivity.this,
                                    new String[]{Manifest.permission.READ_PHONE_STATE},
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                        }
                    })
                    .setIcon(R.drawable.warning_sigh)
                    .show();
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
        }
    }

    private void requestInternetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.INTERNET)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(this)
                    .setTitle("Permission Request")
                    .setMessage("Requesting Permission to Access Internet")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(SplashScreenActivity.this,
                                    new String[]{Manifest.permission.INTERNET},
                                    MY_PERMISSIONS_REQUEST_INTERNET);
                        }
                    })
                    .setIcon(R.drawable.warning_sigh)
                    .show();
        } else {
            // READ_PHONE_STATE permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_INTERNET);
        }
    }



    private class CallAPI extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            String resultToDisplay = "";
            InputStream in = null;
            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
                resultToDisplay = convertStreamToString(in);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
            return resultToDisplay;
        }


    } // end CallAPI

    public String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }


}