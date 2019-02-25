

Here is how i went about mine, I created 2 apps one with trial activity the other without,

i uploaded the one without trial activity to play store as paid app,

and the one with trial activity as free app.

The free app on first launch has options for trial and store purchase, if the user select store purchase it redirects to the store for the user to purchase but if the user clicks trial it take them to the trial activity

NB: I used option 3 like @snctln but with modifications

first, i did not depend on the device time, i got my time from the php file that does the trial registration to the db,

secondly, i used the device serial number to uniquely identify each device,

lastly, the app depends on the time value returned from the server connection not its own time so the system can only be circumvented if the device serial number is changed, which is quite stressful for a user.

so here goes my code (for the Trial activity):

package com.example.mypackage.my_app.Start_Activity.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.onlinewisdom.cbn_app.R;
import com.example.mypackage.my_app.Start_Activity.app.Config;
import com.example.mypackage.my_app.Start_Activity.data.TrialData;
import com.example.mypackage.my_app.Start_Activity.helper.connection.Connection;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Trial extends AppCompatActivity {
    Connection check;
    SweetAlertDialog pDialog;
    TextView tvPleaseWait;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;

    String BASE_URL = Config.BASE_URL;
    String BASE_URL2 = BASE_URL+ "/register_trial/"; //http://ur link to ur API

    //KEY
    public static final String KEY_IMEI = "IMEINumber";

    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    private final long ONE_DAY = 24 * 60 * 60 * 1000;

    SharedPreferences preferences;
    String installDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial);

        preferences = getPreferences(MODE_PRIVATE);
        installDate = preferences.getString("InstallDate", null);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#008753"));
        pDialog.setTitleText("Loading...");
        pDialog.setCancelable(false);

        tvPleaseWait = (TextView) findViewById(R.id.tvPleaseWait);
        tvPleaseWait.setText("");

        if(installDate == null) {
            //register app for trial
            animateLoader(true);
            CheckConnection();
        } else {
            //go to main activity and verify there if trial period is over
            Intent i = new Intent(Trial.this, MainActivity.class);
            startActivity(i);
            // close this activity
            finish();
        }

    }

    public void CheckConnection() {
        check = new Connection(this);
        if (check.isConnected()) {
            //trigger 'loadIMEI'
            loadIMEI();
        } else {
            errorAlert("Check Connection", "Network is not detected");
            tvPleaseWait.setText("Network is not detected");
            animateLoader(false);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Changes 'back' button action
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    public void animateLoader(boolean visibility) {
        if (visibility)
            pDialog.show();
        else
            pDialog.hide();
    }

    public void errorAlert(String title, String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(msg)
                .show();
    }

    /**
     * Called when the 'loadIMEI' function is triggered.
     */
    public void loadIMEI() {
        // Check if the READ_PHONE_STATE permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // READ_PHONE_STATE permission has not been granted.
            requestReadPhoneStatePermission();
        } else {
            // READ_PHONE_STATE permission is already been granted.
            doPermissionGrantedStuffs();
        }
    }


    /**
     * Requests the READ_PHONE_STATE permission.
     * If the permission has been denied previously, a dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestReadPhoneStatePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            new AlertDialog.Builder(Trial.this)
                    .setTitle("Permission Request")
                    .setMessage(getString(R.string.permission_read_phone_state_rationale))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //re-request
                            ActivityCompat.requestPermissions(Trial.this,
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

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {
            // Received permission result for READ_PHONE_STATE permission.est.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // READ_PHONE_STATE permission has been granted, proceed with displaying IMEI Number
                //alertAlert(getString(R.string.permision_available_read_phone_state));
                doPermissionGrantedStuffs();
            } else {
                alertAlert(getString(R.string.permissions_not_granted_read_phone_state));
            }
        }
    }

    private void alertAlert(String msg) {
        new AlertDialog.Builder(Trial.this)
                .setTitle("Permission Request")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do somthing here
                    }
                })
                .setIcon(R.drawable.warning_sigh)
                .show();
    }

    private void successAlert(String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText(msg)
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        // Prepare intent which is to be triggered
                        //Intent i = new Intent(Trial.this, MainActivity.class);
                        //startActivity(i);
                    }
                })
                .show();
    }

    public void doPermissionGrantedStuffs() {
        //Have an  object of TelephonyManager
        TelephonyManager tm =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //Get IMEI Number of Phone  //////////////// for this example i only need the IMEI
        String IMEINumber = tm.getDeviceId();

        /************************************************
         * **********************************************
         * This is just an icing on the cake
         * the following are other children of TELEPHONY_SERVICE
         *
         //Get Subscriber ID
         String subscriberID=tm.getDeviceId();

         //Get SIM Serial Number
         String SIMSerialNumber=tm.getSimSerialNumber();

         //Get Network Country ISO Code
         String networkCountryISO=tm.getNetworkCountryIso();

         //Get SIM Country ISO Code
         String SIMCountryISO=tm.getSimCountryIso();

         //Get the device software version
         String softwareVersion=tm.getDeviceSoftwareVersion()

         //Get the Voice mail number
         String voiceMailNumber=tm.getVoiceMailNumber();


         //Get the Phone Type CDMA/GSM/NONE
         int phoneType=tm.getPhoneType();

         switch (phoneType)
         {
         case (TelephonyManager.PHONE_TYPE_CDMA):
         // your code
         break;
         case (TelephonyManager.PHONE_TYPE_GSM)
         // your code
         break;
         case (TelephonyManager.PHONE_TYPE_NONE):
         // your code
         break;
         }

         //Find whether the Phone is in Roaming, returns true if in roaming
         boolean isRoaming=tm.isNetworkRoaming();
         if(isRoaming)
         phoneDetails+="\nIs In Roaming : "+"YES";
         else
         phoneDetails+="\nIs In Roaming : "+"NO";


         //Get the SIM state
         int SIMState=tm.getSimState();
         switch(SIMState)
         {
         case TelephonyManager.SIM_STATE_ABSENT :
         // your code
         break;
         case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
         // your code
         break;
         case TelephonyManager.SIM_STATE_PIN_REQUIRED :
         // your code
         break;
         case TelephonyManager.SIM_STATE_PUK_REQUIRED :
         // your code
         break;
         case TelephonyManager.SIM_STATE_READY :
         // your code
         break;
         case TelephonyManager.SIM_STATE_UNKNOWN :
         // your code
         break;

         }
         */
        // Now read the desired content to a textview.
        //tvPleaseWait.setText(IMEINumber);
        UserTrialRegistrationTask(IMEINumber);
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    private void UserTrialRegistrationTask(final String IMEINumber) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, BASE_URL2+IMEINumber, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        TrialData result = gson.fromJson(String.valueOf(response), TrialData.class);
                        animateLoader(false);
                        if ("true".equals(result.getError())) {
                            errorAlert("Error", result.getResult());
                            tvPleaseWait.setText("Unknown Error");
                        } else if ("false".equals(result.getError())) {
                            //already created install/trial_start date using the server
                            // so just getting the date called back
                            Date before = null;
                            try {
                                before = (Date)formatter.parse(result.getResult());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            Date now = new Date();
                            assert before != null;
                            long diff = now.getTime() - before.getTime();
                            long days = diff / ONE_DAY;
                            // save the date received
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("InstallDate", String.valueOf(days));
                            // Commit the edits!
                            editor.apply();
                            //go to main activity and verify there if trial period is over
                            Intent i = new Intent(Trial.this, MainActivity.class);
                            startActivity(i);
                            // close this activity
                            finish();
                            //successAlert(String.valueOf(days));
                            //if(days > 5) { // More than 5 days?
                                // Expired !!!
                            //}
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        animateLoader(false);
                        //errorAlert(error.toString());
                        errorAlert("Check Connection", "Could not establish a network connection.");
                        tvPleaseWait.setText("Network is not detected");
                    }
                })

        {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_IMEI, IMEINumber);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }


}