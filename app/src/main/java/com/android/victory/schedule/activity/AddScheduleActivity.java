package com.android.victory.schedule.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.victory.schedule.R;
import com.android.victory.schedule.data.CloudDatabase;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.Schedule;
import com.android.victory.schedule.data.ScheduleDatabase;
import com.android.victory.schedule.service.NetworkService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Time;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class AddScheduleActivity extends AppCompatActivity {

    // id's for timepicker and datepicker dialogs.
    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;

    Spinner salesSpinner,clientSpinner, jobSpinner,serviceSpinner;
    TextView timeTextView, dateTextView;
    EditText meetEditText,descEditText;

    // variables to save the time and date from the time and date picker dialogs.
    int yr, month, day;
    int hour, minute;

    public SQLiteDatabase db;
    public Cursor res;
    private boolean isEdited = false, isDone = false;
    private String doneDate = "";
    private int scheduleIndex = 0;
    public ArrayList<String> salesRowIdList, clientRowIdList, jobRowIdList, serviceRowIdList;
    public ArrayList<String> salesRowNameList, clientRowNameList, jobRowNameList, serviceRowNameList;

    int salSelectIndex = 0, cliSelectIndex = 0, jobSelectIndex = 0, srvSelectIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_schedule);

        salesSpinner = (Spinner)findViewById(R.id.sales_spinner);
        clientSpinner = (Spinner)findViewById(R.id.client_spinner);
        jobSpinner = (Spinner)findViewById(R.id.job_spinner);
        serviceSpinner = (Spinner)findViewById(R.id.service_spinner);
        timeTextView = (TextView)findViewById(R.id.textTime);
        dateTextView = (TextView)findViewById(R.id.textDate);
        meetEditText = (EditText)findViewById(R.id.meet_edittext);
        descEditText = (EditText)findViewById(R.id.desc_edittext);

        if (getIntent().getExtras() != null){
            isEdited = getIntent().getExtras().getBoolean("ISEDIT");
            scheduleIndex = getIntent().getExtras().getInt("INDEX");
            isDone = getIntent().getExtras().getBoolean("ISDONE");
            timeTextView.setText(Model.getInstance().hasEditedSchedule.getSch_date().split("---")[1]);
            dateTextView.setText(Model.getInstance().hasEditedSchedule.getSch_date().split("---")[0]);
            meetEditText.setText(Model.getInstance().hasEditedSchedule.getMeet_name());
            descEditText.setText(Model.getInstance().hasEditedSchedule.getKet());
        }
        if (isDone){
            doneDate = getIntent().getExtras().getString("DONE_DATE");
            salesSpinner.setEnabled(false);
            clientSpinner.setEnabled(false);
            jobSpinner.setEnabled(false);
            serviceSpinner.setEnabled(false);
            meetEditText.setEnabled(false);
            descEditText.setEnabled(false);
        }

        timeTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isDone)return false;
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getRawX() >= (timeTextView.getRight() - timeTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        showDialog(TIME_DIALOG_ID);
                        return true;
                    }
                }
                return false;
            }
        });

        dateTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isDone)return false;
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getRawX() >= (dateTextView.getRight() - dateTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        showDialog(DATE_DIALOG_ID);
                        return true;
                    }
                }
                return false;
            }
        });

        // get the current date and time
        Calendar today = Calendar.getInstance();
        yr = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);
        day = today.get(Calendar.DAY_OF_MONTH);

        hour = today.get(Calendar.HOUR_OF_DAY);
        minute = today.get(Calendar.MINUTE);

        salesRowIdList = new ArrayList<>();
        clientRowIdList = new ArrayList<>();
        jobRowIdList = new ArrayList<>();
        serviceRowIdList = new ArrayList<>();
        salesRowNameList = new ArrayList<>();
        clientRowNameList = new ArrayList<>();
        jobRowNameList = new ArrayList<>();
        serviceRowNameList = new ArrayList<>();
        int index = 0;
        final CloudDatabase cloudDatabase = new CloudDatabase(AddScheduleActivity.this);
        db = cloudDatabase.getReadableDatabase();
        Cursor  cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_SALESMAN_NAME,null);

        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String salRowId = cursor.getString(cursor.getColumnIndex(CloudDatabase.SALESMAN_ROW_ID));
                String salRowName = cursor.getString(cursor.getColumnIndex(CloudDatabase.SALESMAN_NAME));
                if (isEdited){
                    if (salRowName.equals(Model.getInstance().hasEditedSchedule.getSal_name()))salSelectIndex = index;
                }
                salesRowIdList.add(salRowId);
                salesRowNameList.add(salRowName);
                cursor.moveToNext();
                index++;
            }
        }
        if (!salesRowIdList.isEmpty()){
            ArrayAdapter<String> client_adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, salesRowNameList);
            salesSpinner.setAdapter(client_adapter);
            salesSpinner.setSelection(salSelectIndex);
        }else {
            salesRowIdList.add(Model.getInstance().username);
            ArrayAdapter<String> client_adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, salesRowIdList);
            salesSpinner.setAdapter(client_adapter);
            salesSpinner.setSelection(0);
        }

        index = 0;

        cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_CLIENT_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String clientRowId = cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_ROW_ID));
                String clientRowName = cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_NAME));
                if (isEdited){
                    if (clientRowName.equals(Model.getInstance().hasEditedSchedule.getClient_name()))cliSelectIndex = index;
                }
                clientRowIdList.add(clientRowId);
                clientRowNameList.add(clientRowName);
                cursor.moveToNext();
                index++;
            }
        }
        if (!clientRowIdList.isEmpty()){
            ArrayAdapter<String> client_adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, clientRowNameList);
            clientSpinner.setAdapter(client_adapter);
            clientSpinner.setSelection(cliSelectIndex);
        }

        index = 0;

        cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_JOB_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String jobRowId = cursor.getString(cursor.getColumnIndex(CloudDatabase.JOB_ROW_ID));
                String jobName = cursor.getString(cursor.getColumnIndex(CloudDatabase.JOB_NAME));
                if (isEdited){
                    if (jobName.equals(Model.getInstance().hasEditedSchedule.getJob_name()))jobSelectIndex = index;
                }
                jobRowIdList.add(jobRowId);
                jobRowNameList.add(jobName);
                cursor.moveToNext();
                index++;
            }
        }

        if (!jobRowIdList.isEmpty()){
            ArrayAdapter<String> client_adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, jobRowNameList);
            jobSpinner.setAdapter(client_adapter);
            jobSpinner.setSelection(jobSelectIndex);
        }

        index = 0;

        cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_SERVICE_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String serviceRowId = cursor.getString(cursor.getColumnIndex(CloudDatabase.SERVICE_CODE));
                String serviceRowName = cursor.getString(cursor.getColumnIndex(CloudDatabase.SERVICE_NAME));
                if (isEdited){
                    if (serviceRowName.equals(Model.getInstance().hasEditedSchedule.getSrv_name()))srvSelectIndex = index;
                }
                serviceRowIdList.add(serviceRowId);
                serviceRowNameList.add(serviceRowName);
                cursor.moveToNext();
                index++;
            }
        }

        if (!serviceRowIdList.isEmpty()){
            ArrayAdapter<String> client_adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, serviceRowNameList);
            serviceSpinner.setAdapter(client_adapter);
            serviceSpinner.setSelection(srvSelectIndex);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AddScheduleActivity.this, MainActivity.class));
        finish();
    }

    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int hour_minute) {
            hour = hourOfDay;
            minute = hour_minute;

            String time = getTime(hourOfDay, hour_minute);
            timeTextView.setText(time);
        }
    };

    private String getTime(int hr, int min) {
        Time time = new Time(hr, min, 0);
        Format formatter;
        formatter = new SimpleDateFormat("HH:mm");
        return formatter.format(time);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            yr = year;
            month = monthOfYear;
            day = dayOfMonth;

            String dateSet = day + "-" + (month + 1) + "-" + year;
            DateFormat dfFrom = new SimpleDateFormat("dd-MM-yyyy");
            Date inputDate = null;
            try {
                inputDate = dfFrom.parse(dateSet);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            DateFormat dfTo = new SimpleDateFormat("MM-dd-yyyy");
            String outputDate = dfTo.format(inputDate);
            dateTextView.setText(outputDate);
        }
    };

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, mTimeSetListener, hour, minute, false);
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, yr, month, day);
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public void showTimePickerDialog(View view) {
        showDialog(TIME_DIALOG_ID);
    }

    @SuppressWarnings("deprecation")
    public void showDatePickerDialog(View view) {
        showDialog(DATE_DIALOG_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_schedule_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.apply_check) {
            if (timeTextView.getText().toString().isEmpty()){
                timeTextView.setError("Time is needed!!!");
                return false;
            }else if (dateTextView.getText().toString().isEmpty()){
                dateTextView.setError("Date is needed!!!");
                return false;
            }else if (meetEditText.getText().toString().isEmpty()){
                meetEditText.setError("Meet Name is needed!!!");
                return false;
            }else if (descEditText.getText().toString().isEmpty()){
                descEditText.setError("Description is needed!!!");
                return false;
            }

            final ScheduleDatabase scheduleDatabase = new ScheduleDatabase(AddScheduleActivity.this);
            db = scheduleDatabase.getWritableDatabase();
            String rowID = System.currentTimeMillis()/1000 + "";
            if (isEdited){
                Cursor res = db.rawQuery("select * from " + ScheduleDatabase.TABLE_SCHEDULE_NAME, null);
                int index = 0;
                if (res.moveToFirst()){
                    while (!res.isAfterLast()){
                        if (scheduleIndex == index){
                            rowID = res.getString(res.getColumnIndex(ScheduleDatabase.SCHEDULE_ROW_ID));
                            db.delete(ScheduleDatabase.TABLE_SCHEDULE_NAME, ScheduleDatabase.SCHEDULE_ID + " = '" + res.getString(res.getColumnIndex(ScheduleDatabase.SCHEDULE_ID)) + "'",null);
                            break;
                        }
                        index++;
                        res.moveToNext();
                    }
                }
            }
            ContentValues contentValues = new ContentValues();
            contentValues.clear();
            contentValues.put(scheduleDatabase.SCHEDULE_ROW_ID, rowID);
            contentValues.put(scheduleDatabase.SCHEDULE_SAL_ID, salesRowIdList.get(salesSpinner.getSelectedItemPosition()));
            contentValues.put(scheduleDatabase.SCHEDULE_SAL_NAME, salesSpinner.getSelectedItem().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_CLIENT_ID, clientRowIdList.get(clientSpinner.getSelectedItemPosition()));
            contentValues.put(scheduleDatabase.SCHEDULE_CLIENT_NAME, clientSpinner.getSelectedItem().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_JOB_ID, jobRowIdList.get(jobSpinner.getSelectedItemPosition()));
            contentValues.put(scheduleDatabase.SCHEDULE_JOB_NAME, jobSpinner.getSelectedItem().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_SRV_ID, serviceRowIdList.get(serviceSpinner.getSelectedItemPosition()));
            contentValues.put(scheduleDatabase.SCHEDULE_SRV_NAME, serviceSpinner.getSelectedItem().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_KET,descEditText.getText().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_MEET_NAME,meetEditText.getText().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_SCH_DATE, dateTextView.getText().toString() + "---" + timeTextView.getText().toString());
            contentValues.put(scheduleDatabase.SCHEDULE_SYNC_DATE,"");
            contentValues.put(scheduleDatabase.SCHEDULE_DONE_DATE,doneDate);
            db.insert(scheduleDatabase.TABLE_SCHEDULE_NAME, null, contentValues);

            startActivity(new Intent(AddScheduleActivity.this, ScheduleActivity.class));
            finish();
            NetworkService.uploadSchedule(salesRowIdList.get(salesSpinner.getSelectedItemPosition()), clientRowIdList.get(clientSpinner.getSelectedItemPosition()), jobRowIdList.get(jobSpinner.getSelectedItemPosition()), serviceRowIdList.get(serviceSpinner.getSelectedItemPosition())
                    , descEditText.getText().toString(), meetEditText.getText().toString(), dateTextView.getText().toString() + "---" + timeTextView.getText().toString(), rowID);
            return true;
        }else if (id == R.id.apply_cancel){
            startActivity(new Intent(AddScheduleActivity.this, ScheduleActivity.class));
            finish();
        }


        return super.onOptionsItemSelected(item);
    }
}
