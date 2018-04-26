package com.android.victory.schedule.activity;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.victory.schedule.R;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.StateSaveActivity;
import com.android.victory.schedule.service.NetworkService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    public final static String TAG = "LoginActivity";

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;

    public static final int RequestPermissionCode  = 1 ;

    private EditText compText, usernameText, pwdEditText,otpText;
    private TextView signinTextView,otpTextView;
    public LinearLayout accountLayout,otpLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        EnableRuntimePermission();


        compText = (EditText)findViewById(R.id.comp_edittext);
        usernameText = (EditText)findViewById(R.id.username_edittext);
        pwdEditText = (EditText)findViewById(R.id.pwd_edittext);
        signinTextView = (TextView)findViewById(R.id.signin_textview);
        otpText = (EditText) findViewById(R.id.otp_edittext);
        otpTextView = (TextView)findViewById(R.id.otp_textview);
        accountLayout = (LinearLayout)findViewById(R.id.account_Layout);
        otpLayout = (LinearLayout)findViewById(R.id.otp_layout);
        pwdEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        pwdEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (pwdEditText.getRight() - pwdEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        if (pwdEditText.getInputType() != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
                            pwdEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        }else{
                            pwdEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        signinTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (compText.getText().toString().isEmpty()){
                    compText.setError("Comp is needed!");
                    return;
                }

                if (usernameText.getText().toString().isEmpty()){
                    usernameText.setError("User Name is needed!");
                    return;
                }

                if (pwdEditText.getText().toString().isEmpty()){
                    pwdEditText.setError("Password is needed!");
                    return;
                }
                if (Model.getInstance().isNetworkAvailable(LoginActivity.this)){
                    Model.getInstance().isVailable = true;
                    signinTextView.setText("--Signing--");
                    new AsyncLogin().execute(compText.getText().toString(), usernameText.getText().toString(), pwdEditText.getText().toString());

                    /*startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    startService(new Intent(LoginActivity.this,NetworkService.class ));
                    Model.getInstance().username = usernameText.getText().toString();
                    Model.getInstance().comp = compText.getText().toString();
                    Model.getInstance().password = pwdEditText.getText().toString();
                    StateSaveActivity.writeFirstTime(LoginActivity.this, true);
                    StateSaveActivity.writeUserName(LoginActivity.this,usernameText.getText().toString());
                    StateSaveActivity.writePassword(LoginActivity.this,pwdEditText.getText().toString());
                    StateSaveActivity.writeComp(LoginActivity.this,compText.getText().toString());*/
                }else {
                    Model.getInstance().isVailable = false;
                    if (StateSaveActivity.readFirstTime(LoginActivity.this)){
                        Toast.makeText(LoginActivity.this, "Network Failed", Toast.LENGTH_SHORT).show();
                    }else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }

            }
        });

        otpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (otpText.getText().toString().isEmpty()){
                    otpText.setError("OTP is needed!!!");
                    return;
                }
                if (Model.getInstance().isNetworkAvailable(LoginActivity.this)){
                    Model.getInstance().isVailable = true;
                    otpTextView.setText("--Activating--");
                    new AsyncOTP().execute(compText.getText().toString(), otpText.getText().toString());
                }else {
                    Model.getInstance().isVailable = false;
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

            }
        });
        if (!StateSaveActivity.readFirstTime(LoginActivity.this)){
            StateSaveActivity.writeTime(LoginActivity.this, System.currentTimeMillis());
        }
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                Manifest.permission.CAMERA)){

        }else ActivityCompat.requestPermissions(LoginActivity.this,new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
    }

    private class AsyncLogin extends AsyncTask<String, String, String>
    {
        HttpsURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loadingImageView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                //url = new URL(model.domainString + "act.php?login=" + model.activateCode + "&uid=" + model.macAddress);
                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/a_login/" + strings[0] + "/" + strings[1] + "/" + strings[2]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(LoginActivity.this).getSocketFactory());
                int response = conn.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK){
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null)
                    {
                        result.append(line);
                    }
                    return result.toString();
                }else{
                    return "unsuccessful";
                }
            }catch(IOException e)
            {
                e.printStackTrace();
                return "Network Timeout";
            } finally {
                conn.disconnect();

            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        if (jsonArray.length() != 0){
                            Model.getInstance().nickcode = jsonArray.getJSONObject(0).getString("sal_code");
                            Model.getInstance().nickname = jsonArray.getJSONObject(0).getString("sal_name");
                            Model.getInstance().username = usernameText.getText().toString();
                            Model.getInstance().comp = compText.getText().toString();
                            Model.getInstance().password = pwdEditText.getText().toString();
                            StateSaveActivity.writeFirstTime(LoginActivity.this, true);
                            StateSaveActivity.writeUserName(LoginActivity.this,usernameText.getText().toString());
                            StateSaveActivity.writePassword(LoginActivity.this,pwdEditText.getText().toString());
                            StateSaveActivity.writeComp(LoginActivity.this,compText.getText().toString());
                            accountLayout.setVisibility(View.GONE);
                            otpLayout.setVisibility(View.VISIBLE);
                            signinTextView.setText("Sign In");
                            Model.getInstance().displayMode = StateSaveActivity.readMode(LoginActivity.this).isEmpty()?"1":StateSaveActivity.readMode(LoginActivity.this);
                            startService(new Intent(LoginActivity.this,NetworkService.class ));
                        }else{
                            signinTextView.setText("Sign In");
                            Toast.makeText(LoginActivity.this, "Your account is locked...", Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else{
                    signinTextView.setText("Sign In");
                    Toast.makeText(LoginActivity.this, "Your account is locked...", Toast.LENGTH_SHORT).show();
                }

            }else {
                signinTextView.setText("Sign In");
                Toast.makeText(LoginActivity.this, "Network Timeout.", Toast.LENGTH_SHORT).show();
            }

            super.onPostExecute(s);
        }
    }

    private class AsyncOTP extends AsyncTask<String, String, String>
    {
        HttpsURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loadingImageView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/a_otp/" + strings[0] + "/" + strings[1]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(LoginActivity.this).getSocketFactory());
                int response = conn.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK){
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();

                    String line;

                    while ((line = reader.readLine()) != null)
                    {
                        result.append(line);
                    }
                    return result.toString();
                }else{
                    return "unsuccessful";
                }
            }catch(IOException e)
            {
                e.printStackTrace();
                return "Network Timeout";
            }finally {
                conn.disconnect();

            }
        }

        @Override
        protected void onPostExecute(String s) {
            otpTextView.setText("Activate");
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        if (jsonArray.length() != 0){

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this, "Your account is locked...", Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }

            }else {
                otpText.setText("Activate");
                Toast.makeText(LoginActivity.this, "Network Timeout!!!", Toast.LENGTH_SHORT).show();
            }

            super.onPostExecute(s);
        }
    }



}

