package com.android.victory.schedule.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.victory.schedule.R;
import com.android.victory.schedule.activity.LoginActivity;
import com.android.victory.schedule.activity.ScheduleActivity;
import com.android.victory.schedule.data.CloudDatabase;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.ScheduleDatabase;
import com.android.victory.schedule.data.StateSaveActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class NetworkService extends Service {

    private static final String TAG = NetworkService.class.getSimpleName();

    public static final int CONNECTION_TIMEOUT=10000;
    public static final int READ_TIMEOUT=15000;
    public static Timer timer;
    int period = 5000;
    MediaPlayer mp;
    public static Context context;
    public NetworkService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId){

        Log.d(TAG, "Srv Starting...");
        context = getApplicationContext();
        downloadCloud();
        warningDisplay();
        deleteTimerRequest();
        return START_STICKY;
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public void downloadCloud(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //CALL YOUR ASSYNC TASK HERE.
                if (isNetworkAvailable(getApplicationContext())){
                    Model.getInstance().isVailable = true;
                    new AsyncClient().execute(Model.getInstance().comp);
                    new AsyncJob().execute(Model.getInstance().comp);
                    new AsyncSalesMan().execute(Model.getInstance().comp, Model.getInstance().username);
                    new AsyncService().execute(Model.getInstance().comp);
                    new AsyncSchedule().execute();
                }else {
                    Model.getInstance().isVailable = false;
                }
            }
        };
        timer = new Timer();
        //DELAY: the time to the first execution
        //PERIODICAL_TIME: the time between each execution of your task.
        timer.schedule(timerTask, 0,5000);
    }

    public void warningDisplay(){
        mp = MediaPlayer.create(getApplicationContext(), R.raw.warning);
        mp.setLooping(true);
        Timer fakeTimer = new Timer();
        TimerTask fakeTimerTask = new TimerTask() {
            @Override
            public void run() {

                if (!Settings.Secure.getString(getContentResolver(),Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")){

                    period = 500;
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mp.start();
                            Toast toast = Toast.makeText(getApplicationContext(), "   ---Fake GPS Warning---   ", Toast.LENGTH_SHORT);
                            View view = toast.getView();
                            view.setBackgroundResource(R.drawable.red_rect);
                            TextView text = (TextView) view.findViewById(android.R.id.message);
                            toast.show();
                            Log.d(TAG, "warning....");
                            Intent goIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            goIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(goIntent);
                        }
                    });
                }
            }
        };
        fakeTimer.schedule(fakeTimerTask,0,period);
    }

    public void deleteTimerRequest(){
        Timer deleteTimer = new Timer();
        TimerTask deleteTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - StateSaveActivity.readTime(context) > Model.getInstance().localPeriodTime){
                    ScheduleDatabase scheduleDatabase = new ScheduleDatabase(getApplicationContext());
                    SQLiteDatabase db = scheduleDatabase.getReadableDatabase();
                    db.execSQL("delete from " + ScheduleDatabase.TABLE_SCHEDULE_NAME);
                    CloudDatabase cloudDatabase = new CloudDatabase(getApplicationContext());
                    db = cloudDatabase.getReadableDatabase();
                    db.execSQL("delete from " + CloudDatabase.TABLE_SALESMAN_NAME);
                    db.execSQL("delete from " + CloudDatabase.TABLE_CLIENT_NAME);
                    db.execSQL("delete from " + CloudDatabase.TABLE_SERVICE_NAME);
                    db.execSQL("delete from " + CloudDatabase.TABLE_JOB_NAME);
                    StateSaveActivity.writeTime(context, System.currentTimeMillis());
                }

            }
        };
        deleteTimer.schedule(deleteTimerTask,0,Model.getInstance().localPeriodTime);

        Timer scheduletimer = new Timer();
        TimerTask scheduleTimertask = new TimerTask() {
            @Override
            public void run() {
                new AsyncSchedule().execute();
            }
        };
        scheduletimer.schedule(scheduleTimertask, 0,5000);
    }

    public static void uploadSchedule(final String salId, final String clientId, final String jobId, final String srvId, final String ket, final String meetName, final String dateTime, final String rowID){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //CALL YOUR ASSYNC TASK HERE.
                if (isNetworkAvailable(context)){
                    Model.getInstance().isVailable = true;
                    new SchedulePostRequest().execute(rowID,salId,clientId,jobId,srvId,dateTime,meetName,ket);
                }else {
                    Model.getInstance().isVailable = false;
                }
            }
        };
        Timer uploadtimer = new Timer();
        //DELAY: the time to the first execution
        //PERIODICAL_TIME: the time between each execution of your task.
        uploadtimer.schedule(timerTask, 0,3600000);
    }

    public static void doneSchedule(final String rowId){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //CALL YOUR ASSYNC TASK HERE.
                if (isNetworkAvailable(context)){
                    Model.getInstance().isVailable = true;
                    new SchedulePutRequest().execute(rowId);
                }else {
                    Model.getInstance().isVailable = false;
                }
            }
        };
        Timer puttimer = new Timer();
        //DELAY: the time to the first execution
        //PERIODICAL_TIME: the time between each execution of your task.
        puttimer.schedule(timerTask, 0,3600000);
    }

    public class AsyncClient extends AsyncTask<String, String, String>
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

                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/m_client/" + strings[0]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(getApplicationContext()).getSocketFactory());
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
            }finally {
                conn.disconnect();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        if (jsonArray.length() != 0){
                            CloudDatabase cloudDatabase = new CloudDatabase(getApplicationContext());
                            SQLiteDatabase db = cloudDatabase.getWritableDatabase();
                            for (int i = 0 ; i < jsonArray.length(); i++){
                                String row_id = jsonArray.getJSONObject(i).has("row_id")?jsonArray.getJSONObject(i).getString("row_id"):"null";
                                String client_code = jsonArray.getJSONObject(i).has("client_code")?jsonArray.getJSONObject(i).getString("client_code"):"null";
                                String client_name = jsonArray.getJSONObject(i).has("client_name")?jsonArray.getJSONObject(i).getString("client_name"):"null";
                                String addr = jsonArray.getJSONObject(i).has("addr")?jsonArray.getJSONObject(0).getString("addr"):"null";
                                String phone = jsonArray.getJSONObject(i).has("phone")?jsonArray.getJSONObject(0).getString("phone"):"null";
                                String hp = jsonArray.getJSONObject(i).has("hp")?jsonArray.getJSONObject(i).getString("hp"):"null";
                                String geo_latt = jsonArray.getJSONObject(i).has("geo_latt")?jsonArray.getJSONObject(i).getString("geo_latt"):"null";
                                String geo_long = jsonArray.getJSONObject(i).has("geo_long")?jsonArray.getJSONObject(i).getString("geo_long"):"null";
                                String ket = jsonArray.getJSONObject(i).has("ket")?jsonArray.getJSONObject(i).getString("ket"):"null";
                                String sync_date = jsonArray.getJSONObject(i).has("sync_date")?jsonArray.getJSONObject(i).getString("sync_date"):"null";
                                String update_user = jsonArray.getJSONObject(i).has("update_user")?jsonArray.getJSONObject(i).getString("update_user"):"null";
                                String update_date = jsonArray.getJSONObject(i).has("update_date")?jsonArray.getJSONObject(i).getString("update_date"):"null";

                                Cursor res = db.rawQuery( "select * from " + CloudDatabase.TABLE_CLIENT_NAME + " where "
                                        + cloudDatabase.CLIENT_NAME + " = '" + client_name + "' and "
                                        + cloudDatabase.CLIENT_CODE + " = '" + client_code +"'", null);
                                int count = res.getColumnCount();
                                if (res.getCount() <= 0){
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.clear();
                                    contentValues.put(cloudDatabase.CLIENT_ROW_ID, row_id.isEmpty()?"null":row_id);
                                    contentValues.put(cloudDatabase.CLIENT_CODE, client_code.isEmpty()?"null":client_code);
                                    contentValues.put(cloudDatabase.CLIENT_NAME, client_name.isEmpty()?"null":client_name);
                                    contentValues.put(cloudDatabase.CLIENT_ADDRESS,addr.isEmpty()?"null":addr);
                                    contentValues.put(cloudDatabase.CLIENT_PHONE,phone.isEmpty()?"null":phone);
                                    contentValues.put(cloudDatabase.CLIENT_HP,hp.isEmpty()?"null":hp);
                                    contentValues.put(cloudDatabase.CLIENT_GEO_LATT,geo_latt.isEmpty()?"null":geo_latt);
                                    contentValues.put(cloudDatabase.CLIENT_GEO_LONG,geo_long.isEmpty()?"null":geo_long);
                                    contentValues.put(cloudDatabase.CLIENT_KET,ket.isEmpty()?"null":ket);
                                    contentValues.put(cloudDatabase.CLIENT_SYNC_DATE,sync_date.isEmpty()?"null":sync_date);
                                    contentValues.put(cloudDatabase.CLIENT_UPDATE_USER,update_user.isEmpty()?"null":update_user);
                                    contentValues.put(cloudDatabase.CLIENT_UPDATE_DATE,update_date.isEmpty()?"null":update_date);
                                    db.insert(cloudDatabase.TABLE_CLIENT_NAME, null, contentValues);
                                }
                                res.close();
                            }
                        }else{

                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else {

                }
            } else {
                Toast.makeText(NetworkService.this, "You can't access to oracle cloud because of network.", Toast.LENGTH_SHORT).show();
            }


            super.onPostExecute(s);
        }
    }
    public class AsyncJob extends AsyncTask<String, String, String>
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
                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/m_job/" + strings[0]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(getApplicationContext()).getSocketFactory());
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
            }finally {
                conn.disconnect();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        CloudDatabase cloudDatabase = new CloudDatabase(getApplicationContext());
                        SQLiteDatabase db = cloudDatabase.getWritableDatabase();

                        if (jsonArray.length() != 0){
                            for (int i = 0 ; i < jsonArray.length() ; i++){
                                String row_id = jsonArray.getJSONObject(i).has("row_id")?jsonArray.getJSONObject(i).getString("row_id"):"null";
                                String job_code = jsonArray.getJSONObject(i).has("job_code")?jsonArray.getJSONObject(i).getString("job_code"):"null";
                                String job_name = jsonArray.getJSONObject(i).has("job_name")?jsonArray.getJSONObject(i).getString("job_name"):"null";
                                String ket = jsonArray.getJSONObject(i).has("ket")?jsonArray.getJSONObject(i).getString("ket"):"null";
                                String update_user = jsonArray.getJSONObject(i).has("update_user")?jsonArray.getJSONObject(i).getString("update_user"):"null";
                                String update_date = jsonArray.getJSONObject(i).has("update_date")?jsonArray.getJSONObject(i).getString("update_date"):"null";

                                Cursor res = db.rawQuery( "select * from " + CloudDatabase.TABLE_JOB_NAME + " where "
                                        + cloudDatabase.JOB_NAME + " = '" + job_name + "' and "
                                        + cloudDatabase.JOB_CODE + " = '" + job_code +"'", null);
                                if (res.getCount() <= 0){
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.clear();
                                    contentValues.put(cloudDatabase.JOB_ROW_ID, row_id.isEmpty()?"null":row_id);
                                    contentValues.put(cloudDatabase.JOB_CODE, job_code.isEmpty()?"null":job_code);
                                    contentValues.put(cloudDatabase.JOB_NAME, job_name.isEmpty()?"null":job_name);
                                    contentValues.put(cloudDatabase.JOB_KET,update_date.isEmpty()?"null":ket);
                                    contentValues.put(cloudDatabase.JOB_UPDATE_USER,update_date.isEmpty()?"null":update_user);
                                    contentValues.put(cloudDatabase.JOB_UPDATE_DATE,update_date.isEmpty()?"null":update_date);
                                    db.insert(cloudDatabase.TABLE_JOB_NAME, null, contentValues);
                                }
                                res.close();
                            }
                        }else{

                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else {

                }
            }else {
                Toast.makeText(NetworkService.this, "You can't access to oracle cloud because of network.", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }
    public class AsyncSalesMan extends AsyncTask<String, String, String>
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
                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/m_salesman/" + strings[0] + "/" + strings[1]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(getApplicationContext()).getSocketFactory());
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
            }finally {
                conn.disconnect();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        CloudDatabase cloudDatabase = new CloudDatabase(getApplicationContext());
                        SQLiteDatabase db = cloudDatabase.getWritableDatabase();
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        if (jsonArray.length() != 0){
                            for (int i = 0 ; i < jsonArray.length() ; i++){
                                String row_id = jsonArray.getJSONObject(i).has("row_id")?jsonArray.getJSONObject(i).getString("row_id"):"null";
                                String sal_code = jsonArray.getJSONObject(i).has("sal_code")?jsonArray.getJSONObject(i).getString("sal_code"):"null";
                                String sal_name = jsonArray.getJSONObject(i).has("sal_name")?jsonArray.getJSONObject(i).getString("sal_name"):"null";
                                String pict_data = jsonArray.getJSONObject(i).has("pict_data")?jsonArray.getJSONObject(i).getString("pict_data"):"null";
                                String self_flag = jsonArray.getJSONObject(i).has("self_flag")?jsonArray.getJSONObject(i).getString("self_flag"):"null";
                                String addr = jsonArray.getJSONObject(i).has("addr")?jsonArray.getJSONObject(0).getString("addr"):"null";
                                String phone = jsonArray.getJSONObject(i).has("phone")?jsonArray.getJSONObject(0).getString("phone"):"null";
                                String hp = jsonArray.getJSONObject(i).has("hp")?jsonArray.getJSONObject(i).getString("hp"):"null";
                                String geo_latt = jsonArray.getJSONObject(i).has("geo_latt")?jsonArray.getJSONObject(i).getString("geo_latt"):"null";
                                String geo_long = jsonArray.getJSONObject(i).has("geo_long")?jsonArray.getJSONObject(i).getString("geo_long"):"null";
                                String ket = jsonArray.getJSONObject(i).has("ket")?jsonArray.getJSONObject(i).getString("ket"):"null";
                                String sync_flag = jsonArray.getJSONObject(i).has("sync_flag")?jsonArray.getJSONObject(i).getString("sync_flag"):"null";
                                String sync_date = jsonArray.getJSONObject(i).has("sync_date")?jsonArray.getJSONObject(i).getString("sync_date"):"null";
                                String update_user = jsonArray.getJSONObject(i).has("update_user")?jsonArray.getJSONObject(i).getString("update_user"):"";
                                String update_date = jsonArray.getJSONObject(i).has("update_date")?jsonArray.getJSONObject(i).getString("update_date"):"";

                                Cursor res = db.rawQuery( "select * from " + CloudDatabase.TABLE_SALESMAN_NAME + " where "
                                        + cloudDatabase.SALESMAN_NAME + " = '" + sal_name + "' and "
                                        + cloudDatabase.SALESMAN_CODE + " = '" + sal_code +"'", null);
                                if (res.getCount() <= 0){
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.clear();
                                    contentValues.put(cloudDatabase.SALESMAN_ROW_ID, row_id.isEmpty()?"null":row_id);
                                    contentValues.put(cloudDatabase.SALESMAN_CODE, sal_code.isEmpty()?"null":sal_code);
                                    contentValues.put(cloudDatabase.SALESMAN_NAME, sal_name.isEmpty()?"null":sal_name);
                                    contentValues.put(cloudDatabase.SALESMAN_PICT_DATA, sal_name.isEmpty()?"null":pict_data);
                                    contentValues.put(cloudDatabase.SALESMAN_SELF_FLAG, sal_name.isEmpty()?"null":self_flag);
                                    contentValues.put(cloudDatabase.SALESMAN_ADDRESS,addr.isEmpty()?"null":addr);
                                    contentValues.put(cloudDatabase.SALESMAN_PHONE,phone.isEmpty()?"null":phone);
                                    contentValues.put(cloudDatabase.SALESMAN_HP,hp.isEmpty()?"null":hp);
                                    contentValues.put(cloudDatabase.SALESMAN_GEO_LATT,geo_latt.isEmpty()?"null":geo_latt);
                                    contentValues.put(cloudDatabase.SALESMAN_GEO_LONG,geo_long.isEmpty()?"null":geo_long);
                                    contentValues.put(cloudDatabase.SALESMAN_KET,ket.isEmpty()?"null":ket);
                                    contentValues.put(cloudDatabase.SALESMAN_SYNC_FLAG,sync_flag.isEmpty()?"null":sync_flag);
                                    contentValues.put(cloudDatabase.SALESMAN_SYNC_DATE,sync_date.isEmpty()?"null":sync_date);
                                    contentValues.put(cloudDatabase.SALESMAN_UPDATE_USER,update_user.isEmpty()?"null":update_user);
                                    contentValues.put(cloudDatabase.SALESMAN_UPDATE_DATE ,update_date.isEmpty()?"null":update_date);
                                    db.insert(cloudDatabase.TABLE_SALESMAN_NAME, null, contentValues);
                                }
                                res.close();
                            }
                            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }else{

                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else {

                }
            }else {
                Toast.makeText(NetworkService.this, "You can't access to oracle cloud because of network.", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }
    public class AsyncService extends AsyncTask<String, String, String>
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
                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/m_srv/" + strings[0]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(getApplicationContext()).getSocketFactory());
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
            }finally {
                conn.disconnect();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        if (jsonArray.length() != 0){
                            CloudDatabase cloudDatabase = new CloudDatabase(getApplicationContext());
                            SQLiteDatabase db = cloudDatabase.getWritableDatabase();
                            for (int i = 0 ; i < jsonArray.length() ; i++){
                                String row_id = jsonArray.getJSONObject(i).has("row_id")?jsonArray.getJSONObject(i).getString("row_id"):"null";
                                String service_code = jsonArray.getJSONObject(i).has("srv_code")?jsonArray.getJSONObject(i).getString("srv_code"):"null";
                                String service_name = jsonArray.getJSONObject(i).has("srv_name")?jsonArray.getJSONObject(i).getString("srv_name"):"null";
                                String ket = jsonArray.getJSONObject(i).has("ket")?jsonArray.getJSONObject(i).getString("ket"):"null";
                                String update_user = jsonArray.getJSONObject(i).has("update_user")?jsonArray.getJSONObject(i).getString("update_user"):"null";
                                String update_date = jsonArray.getJSONObject(i).has("update_date")?jsonArray.getJSONObject(i).getString("update_date"):"null";

                                Cursor res = db.rawQuery( "select * from " + CloudDatabase.TABLE_SERVICE_NAME + " where "
                                        + cloudDatabase.SERVICE_NAME + " = '" + service_code + "' and "
                                        + cloudDatabase.SERVICE_CODE + " = '" + service_name +"'", null);
                                if (res.getCount() <= 0){
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.clear();
                                    contentValues.put(cloudDatabase.SERVICE_ROW_ID, row_id.isEmpty()?"null":row_id);
                                    contentValues.put(cloudDatabase.SERVICE_NAME, service_code.isEmpty()?"null":service_code);
                                    contentValues.put(cloudDatabase.SERVICE_CODE, service_name.isEmpty()?"null":service_name);
                                    contentValues.put(cloudDatabase.SERVICE_KET,ket.isEmpty()?"null":ket);
                                    contentValues.put(cloudDatabase.SERVICE_UPDATE_USER,update_user.isEmpty()?"null":update_user);
                                    contentValues.put(cloudDatabase.SERVICE_UPDATE_DATE,update_date.isEmpty()?"null":update_date);
                                    db.insert(cloudDatabase.TABLE_SERVICE_NAME, null, contentValues);
                                }
                                res.close();
                            }
                            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }else{

                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else {

                }
            }else{
                Toast.makeText(NetworkService.this, "You can't access to oracle cloud because of network.", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }
    public class AsyncSchedule extends AsyncTask<String, String, String>
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
                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/tp_schedule_get/" + Model.getInstance().comp + "/" + Model.getInstance().username);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(7200000);
                conn.setConnectTimeout(5400000);
                conn.setRequestMethod("GET");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(getApplicationContext()).getSocketFactory());
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
            }finally {
                conn.disconnect();

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null){
                if (!s.equals("unsuccessful")){
                    try {
                        JSONArray jsonArray = new JSONObject(s).getJSONArray("items");
                        if (jsonArray.length() != 0){
                            ScheduleDatabase scheduleDatabase = new ScheduleDatabase(getApplicationContext());
                            SQLiteDatabase db = scheduleDatabase.getWritableDatabase();
                            for (int i = 0 ; i < jsonArray.length() ; i++){
                                String row_id = jsonArray.getJSONObject(i).has("row_id")?jsonArray.getJSONObject(i).getString("row_id"):"null";
                                String salId = jsonArray.getJSONObject(i).has("row_id_sal")?jsonArray.getJSONObject(i).getString("row_id_sal"):"null";
                                String clientId = jsonArray.getJSONObject(i).has("row_id_client")?jsonArray.getJSONObject(i).getString("row_id_client"):"null";
                                String srvId = jsonArray.getJSONObject(i).has("row_id_service")?jsonArray.getJSONObject(i).getString("row_id_service"):"null";
                                String jobId = jsonArray.getJSONObject(i).has("row_id_job")?jsonArray.getJSONObject(i).getString("row_id_job"):"null";
                                String schDate = jsonArray.getJSONObject(i).has("sch_date")?jsonArray.getJSONObject(i).getString("sch_date"):"null";
                                String meetName = jsonArray.getJSONObject(i).has("meet_name")?jsonArray.getJSONObject(i).getString("meet_name"):"null";
                                String ket = jsonArray.getJSONObject(i).has("ket")?jsonArray.getJSONObject(i).getString("ket"):"null";
                                String done_date = jsonArray.getJSONObject(i).has("done_date")?jsonArray.getJSONObject(i).getString("done_date"):"null";
                                String sync_date = jsonArray.getJSONObject(i).has("sync_date")?jsonArray.getJSONObject(i).getString("sync_date"):"null";

                                Cursor res = db.rawQuery( "select * from " + ScheduleDatabase.TABLE_SCHEDULE_NAME + " where "
                                        + scheduleDatabase.SCHEDULE_CLIENT_ID + " = '" + clientId + "' and "
                                        + scheduleDatabase.SCHEDULE_ROW_ID + " = '" + row_id +"'", null);
                                if (res.getCount() <= 0){
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.clear();
                                    contentValues.put(scheduleDatabase.SCHEDULE_ROW_ID, row_id.isEmpty()?"null":row_id);
                                    contentValues.put(scheduleDatabase.SCHEDULE_SAL_ID, salId.isEmpty()?"null":salId);
                                    contentValues.put(scheduleDatabase.SCHEDULE_CLIENT_ID, clientId.isEmpty()?"null":clientId);
                                    contentValues.put(scheduleDatabase.SCHEDULE_SRV_ID,srvId.isEmpty()?"null":srvId);
                                    contentValues.put(scheduleDatabase.SCHEDULE_JOB_ID,jobId.isEmpty()?"null":jobId);
                                    contentValues.put(scheduleDatabase.SCHEDULE_SCH_DATE,schDate.isEmpty()?"null":schDate);
                                    contentValues.put(scheduleDatabase.SCHEDULE_MEET_NAME,meetName.isEmpty()?"null":meetName);
                                    contentValues.put(scheduleDatabase.SCHEDULE_KET,ket.isEmpty()?"null":ket);
                                    contentValues.put(scheduleDatabase.SCHEDULE_DONE_DATE,done_date.isEmpty()?"null":done_date);
                                    contentValues.put(scheduleDatabase.SCHEDULE_SYNC_DATE,sync_date.isEmpty()?"null":sync_date);
                                    db.insert(scheduleDatabase.TABLE_SCHEDULE_NAME, null, contentValues);
                                }
                                res.close();
                            }

                            //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }else{

                        }
                    }catch (JSONException e){

                        e.printStackTrace();
                    }
                }else {

                }
            }else{
                Toast.makeText(NetworkService.this, "You can't access to oracle cloud because of network.", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }
    }

    public static class SchedulePostRequest extends AsyncTask<String, Void, String> {
        HttpsURLConnection conn;
        URL url = null;
        protected void onPreExecute(){
        }

        protected String doInBackground(String... strings) {

            try {

                URL url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/tp_schedule_post/" + Model.getInstance().comp + "/" + strings[0] + "/" + strings[1] + "/" + strings[2] + "/"
                                        + strings[3] + "/" + strings[4] + "/" + strings[5] + "/" + strings[6] + "/" + strings[7]); // here is your URL path
                //URL url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/tp_schedule_post/1003/932/H3J20d1YBI/124/1/2/20180215/asdasd/asdasdasd");

                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(7200000);
                conn.setConnectTimeout(5400000);
                conn.setRequestMethod("POST");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(context).getSocketFactory());
                int response = conn.getResponseCode();

                if (response == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return "unsuccess";
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            /*Toast.makeText(context, result,
                    Toast.LENGTH_LONG).show();*/
            if (!result.equals("unsuccess")){
                try {
                    JSONObject jsonObject =  new JSONObject(result);
                    String echoString = jsonObject.getString("echo");
                    if (echoString.equals("Exist")){
                        Toast.makeText(context, "The schedule have already existed", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static class SchedulePutRequest extends AsyncTask<String, Void, String> {

        HttpsURLConnection conn = null;
        URL url = null;
        protected void onPreExecute(){
        }

        protected String doInBackground(String... strings) {

            try {

                url = new URL("https://www.myquantumhr.com/ords/pdb1/gluon/data/tp_schedule/" + Model.getInstance().comp + "/" + strings[0]);
                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(12000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("PUT");
                conn.setSSLSocketFactory(Model.getInstance().getSSLContext(context).getSocketFactory());
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
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            /*Toast.makeText(context, result,
                    Toast.LENGTH_LONG).show();*/

        }
    }

    public static String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }
}
