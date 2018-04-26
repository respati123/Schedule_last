package com.android.victory.schedule.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.victory.schedule.ExcelSQlite.ExcelToSQLite;
import com.android.victory.schedule.ExcelSQlite.SQLiteToExcel;
import com.android.victory.schedule.activity.CustomCalendarView.CustomWeekCalendar;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.WeekView;
import com.android.victory.schedule.data.Client;
import com.android.victory.schedule.data.CloudDatabase;

import com.android.victory.schedule.R;
import com.android.victory.schedule.activity.CustomCalendarView.CustomCalendar;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.Badge;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.CalenderDate;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.ClickInterface;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.SalesMan;
import com.android.victory.schedule.data.Schedule;
import com.android.victory.schedule.data.ScheduleDatabase;
import com.android.victory.schedule.data.StateSaveActivity;
import com.android.victory.schedule.service.NetworkService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import static com.android.victory.schedule.service.NetworkService.doneSchedule;


public class ScheduleActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    CustomCalendar scheduleCalendarView;
    CustomWeekCalendar scheduleCalendarWeekView;
    ListView scheduleListView;
    SQLiteDatabase db;
    Cursor cursor;
    ArrayList<Schedule> scheduleArrayList = new ArrayList<>();
    ArrayList<Schedule> tempArrayList = new ArrayList<>();
    ArrayList<SalesMan> salesmanArrayList;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ScheduleActivity.this, AddScheduleActivity.class));
                finish();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        SwitchCompat actionView = (SwitchCompat) menu.findItem(R.id.nav_switch).getActionView().findViewById(R.id.ontoSwitch);
        actionView.setChecked(Model.getInstance().isFlag);
        actionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                displayMode();
                if (isChecked)Model.getInstance().isFlag = true;
                else Model.getInstance().isFlag = false;
                finish();
                startActivity(getIntent());
            }
        });

        ListView salesmanView = (ListView)findViewById(R.id.salesman_list_drawer);

        scheduleCalendarView = (CustomCalendar)findViewById(R.id.calendarview);
        scheduleCalendarView.setFullScreenWidth(true);

        scheduleCalendarWeekView = (CustomWeekCalendar)findViewById(R.id.calendar_weekview);
        scheduleListView = (ListView)findViewById(R.id.client_schedule_listview);

        displayMode();

        //adding badges to the dates.
        final ScheduleDatabase scheduleDatabase = new ScheduleDatabase(ScheduleActivity.this);
        db = scheduleDatabase.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + ScheduleDatabase.TABLE_SCHEDULE_NAME,null);
        int count = cursor.getCount();
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                Schedule schedule = new Schedule();
                String rowId = cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_ROW_ID));
                schedule.set_id(rowId);
                schedule.setComp(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_COMP)));
                schedule.setKet(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_KET)));
                schedule.setMeet_name(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_MEET_NAME)));
                schedule.setClient_id(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_CLIENT_ID)));
                String clientName = getClientName(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_CLIENT_ID)));
                schedule.setClient_name(clientName);
                schedule.setJob_id(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_JOB_ID)));
                String jobName = getJobName(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_JOB_ID)));
                schedule.setJob_name(jobName);
                schedule.setSal_id(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_SAL_ID)));
                String salName = getSalName(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_SAL_ID)));
                schedule.setSal_name(salName);
                schedule.setSrv_id(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_SRV_ID)));
                String srvName = getSrvName(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_SRV_ID)));
                schedule.setSrv_name(srvName);
                schedule.setSch_date(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_SCH_DATE)));
                schedule.setSync_date(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_SYNC_DATE)));
                schedule.setDone_date(cursor.getString(cursor.getColumnIndex(ScheduleDatabase.SCHEDULE_DONE_DATE)));
                tempArrayList.add(schedule);
                cursor.moveToNext();
            }
        }

        scheduleArrayList = tempArrayList;

        scheduleCalendarView.setBadgeDateList(getBadge(scheduleArrayList));

        // implementing onClickListener for dates.
        scheduleCalendarView.setOnClickDate(new ClickInterface() {
            @Override
            public void setDateClicked(CalenderDate date, Schedule schedule) {
                showScheduleMenu(fab, date, schedule);

            }
        });

        scheduleCalendarWeekView.setTotalScheduleArrayList(scheduleArrayList);
        scheduleCalendarWeekView.setOnDateSelected(new WeekView.OnDaySelected() {
            @Override
            public void onSelected(Schedule schedule) {
                CalenderDate date = new CalenderDate();
                showScheduleMenu(fab, date, schedule);
            }
        });

        final CloudDatabase cloudDatabase = new CloudDatabase(ScheduleActivity.this);
        //clientList = new ArrayList<>();
        salesmanArrayList = new ArrayList<>();
        SalesMan salesMan = new SalesMan();
        salesMan.setSal_name("---All---");
        salesmanArrayList.add(salesMan);
        db = cloudDatabase.getReadableDatabase();
        cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_SALESMAN_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                salesMan = new SalesMan();
                salesMan.setSal_name(cursor.getString(cursor.getColumnIndex(CloudDatabase.SALESMAN_NAME)));
                salesmanArrayList.add(salesMan);
                //clientList.add(cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_NAME)));
                cursor.moveToNext();
            }
        }

        ScheduleListAdapter scheduleListAdapter = new ScheduleListAdapter(this, getSortScheduleArrayList(scheduleArrayList), Model.getInstance().isFlag);
        scheduleListView.setAdapter(scheduleListAdapter);
        scheduleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showScheduleMenu(fab, new CalenderDate(),getSortScheduleArrayList(scheduleArrayList).get(position));
            }
        });

        final ListAdapter listAdapter = new ListAdapter(this, salesmanArrayList);
        salesmanView.setAdapter(listAdapter);
        salesmanView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ScheduleActivity.this, listAdapter.getData().get(position).getSal_name(), Toast.LENGTH_SHORT).show();
                ArrayList<Schedule> tempArrayList = new ArrayList<>();
                if (position == 0)tempArrayList = scheduleArrayList;
                else {
                    for (int i = 0 ; i < scheduleArrayList.size() ; i++){
                        if (listAdapter.getData().get(position).getSal_name().equals(scheduleArrayList.get(i).getSal_name())){
                            tempArrayList.add(scheduleArrayList.get(i));
                        }
                    }
                }
                scheduleCalendarView.setBadgeDateList(getBadge(tempArrayList));
                scheduleCalendarWeekView.setTotalScheduleArrayList(tempArrayList);
                ScheduleListAdapter scheduleListAdapter = new ScheduleListAdapter(ScheduleActivity.this, getSortScheduleArrayList(tempArrayList), Model.getInstance().isFlag);
                scheduleListView.setAdapter(scheduleListAdapter);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    class StringDateComparator implements Comparator<String>
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        public int compare(String lhs, String rhs)
        {
            int result = 0;
            try {
                result = dateFormat.parse(lhs).compareTo(dateFormat.parse(rhs));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

    private ArrayList<Schedule> getSortScheduleArrayList(ArrayList<Schedule> scheduleArrayList){
        ArrayList<Schedule> tempScheduleArraylist = new ArrayList<>();
        ArrayList<String> dateArrayList = new ArrayList<>();
        for (int i = 0 ; i < scheduleArrayList.size(); i++){
            dateArrayList.add(scheduleArrayList.get(i).getSch_date().split("---")[0]);
        }
        Collections.sort(dateArrayList, new StringDateComparator());
        for (int i = 0 ; i < dateArrayList.size() ; i++){
            for (int j = 0 ; j < scheduleArrayList.size() ;j++){
                if (scheduleArrayList.get(j).getSch_date().split("---")[0].equals(dateArrayList.get(i))){
                    tempScheduleArraylist.add(scheduleArrayList.get(j));
                }
            }
        }
        return tempScheduleArraylist;
    }

    private String getSalName(String rowId){
        final CloudDatabase cloudDatabase = new CloudDatabase(ScheduleActivity.this);
        db = cloudDatabase.getReadableDatabase();
        scheduleArrayList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_SALESMAN_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String cursorRowID = cursor.getString(cursor.getColumnIndex(CloudDatabase.SALESMAN_ROW_ID));
                if (rowId.equals(cursorRowID))return cursor.getString(cursor.getColumnIndex(CloudDatabase.SALESMAN_NAME));
                else cursor.moveToNext();
            }
        }
        return "";
    }

    private String getClientName(String rowId){
        final CloudDatabase cloudDatabase = new CloudDatabase(ScheduleActivity.this);
        db = cloudDatabase.getReadableDatabase();
        scheduleArrayList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_CLIENT_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String cursorRowID = cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_ROW_ID));
                if (rowId.equals(cursorRowID))return cursor.getString(cursor.getColumnIndex(CloudDatabase.CLIENT_NAME));
                else cursor.moveToNext();
            }
        }
        return "";
    }

    private String getJobName(String rowId){
        final CloudDatabase cloudDatabase = new CloudDatabase(ScheduleActivity.this);
        db = cloudDatabase.getReadableDatabase();
        scheduleArrayList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_JOB_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String cursorRowID = cursor.getString(cursor.getColumnIndex(CloudDatabase.JOB_ROW_ID));
                if (rowId.equals(cursorRowID))return cursor.getString(cursor.getColumnIndex(CloudDatabase.JOB_NAME));
                else cursor.moveToNext();
            }
        }
        return "";
    }

    private String getSrvName(String rowId){
        final CloudDatabase cloudDatabase = new CloudDatabase(ScheduleActivity.this);
        db = cloudDatabase.getReadableDatabase();
        scheduleArrayList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + CloudDatabase.TABLE_SERVICE_NAME,null);
        if (cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String cursorRowID = cursor.getString(cursor.getColumnIndex(CloudDatabase.SERVICE_ROW_ID));
                if (rowId.equals(cursorRowID))return cursor.getString(cursor.getColumnIndex(CloudDatabase.SERVICE_NAME));
                else cursor.moveToNext();
            }
        }
        return "";
    }

    public void displayMode(){
        if (Model.getInstance().displayMode.equals("3")){
            scheduleListView.setVisibility(View.INVISIBLE);
            scheduleCalendarWeekView.setVisibility(View.INVISIBLE);
            scheduleCalendarView.setVisibility(View.VISIBLE);
        }else if (Model.getInstance().displayMode.equals("2")){
            scheduleListView.setVisibility(View.INVISIBLE);
            scheduleCalendarView.setVisibility(View.INVISIBLE);
            scheduleCalendarWeekView.setVisibility(View.VISIBLE);
        }else if (Model.getInstance().displayMode.equals("1")){
            scheduleListView.setVisibility(View.VISIBLE);
            scheduleCalendarView.setVisibility(View.INVISIBLE);
            scheduleCalendarWeekView.setVisibility(View.INVISIBLE);
        }
    }

    public class ListAdapter extends BaseAdapter {

        Context context;
        ArrayList<SalesMan> salesManArrayList;
        public ListAdapter(Context context, ArrayList<SalesMan> salesManArrayList){
            this.context = context;
            this.salesManArrayList = salesManArrayList;
        }

        @Override
        public int getCount() {
            return salesManArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return salesManArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public ArrayList<SalesMan> getData(){
            return salesManArrayList;
        }


        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.client_list, parent, false);
            }
            TextView clientName = (TextView)convertView.findViewById(R.id.client_name);
            clientName.setText(salesManArrayList.get(position).getSal_name());
            return convertView;
        }

    }

    public class ScheduleListAdapter extends BaseAdapter {

        Context context;
        ArrayList<Schedule> scheduleArrayList;
        boolean flag;
        public ScheduleListAdapter(Context context, ArrayList<Schedule> scheduleArrayList,boolean flag){
            this.context = context;
            this.scheduleArrayList = scheduleArrayList;
            this.flag = flag;
        }

        @Override
        public int getCount() {
            return scheduleArrayList.size();
        }

        @Override
        public Object getItem(int i) {
            return scheduleArrayList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public ArrayList<Schedule> getData(){
            return scheduleArrayList;
        }


        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.schedule_list, parent, false);
            }
            TextView scheduleTime = (TextView)convertView.findViewById(R.id.schedule_time_textview);
            TextView clientName = (TextView)convertView.findViewById(R.id.schedule_client_name);
            try {
                Date date = new SimpleDateFormat("MM-dd-yyyy").parse(scheduleArrayList.get(position).getSch_date());
                String outputDateStr = new SimpleDateFormat("MMM-dd-yyyy").format(date);
                scheduleTime.setText(outputDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (scheduleArrayList.get(position).getDone_date().isEmpty() && milliseconds(scheduleArrayList.get(position).getSch_date()) < System.currentTimeMillis()){
                clientName.setBackground(context.getResources().getDrawable(R.drawable.red_rect));
            }else if (!scheduleArrayList.get(position).getDone_date().isEmpty() && milliseconds(scheduleArrayList.get(position).getDone_date()) < System.currentTimeMillis()){
                if (!flag)clientName.setBackground(context.getResources().getDrawable(R.drawable.green_rect));
                else clientName.setVisibility(View.GONE);
            }else if (scheduleArrayList.get(position).getDone_date().isEmpty() && milliseconds(scheduleArrayList.get(position).getSch_date()) >= System.currentTimeMillis()){
                if (!flag)clientName.setBackground(context.getResources().getDrawable(R.drawable.blue_rect));
                else clientName.setVisibility(View.GONE);
            }
            clientName.setText(scheduleArrayList.get(position).getClient_name());
            return convertView;
        }

    }

    private long milliseconds(String date)
    {
        //String date_ = date;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        try
        {
            Date mDate = sdf.parse(date);
            long timeInMilliseconds = mDate.getTime();
            System.out.println("Date in milli :: " + timeInMilliseconds);
            return timeInMilliseconds;
        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    public List<Badge> getBadge(ArrayList<Schedule> scheduleArrayList){
        List<Badge> badges = new ArrayList<>();

        for (int i = 0 ; i < scheduleArrayList.size(); i++){
            try {
                ArrayList<Schedule> subSchedule = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                Date mDate = sdf.parse(scheduleArrayList.get(i).getSch_date());
                Calendar cal = Calendar.getInstance();
                cal.setTime(mDate);
                int day = cal.get(Calendar.DATE);
                int month = cal.get(Calendar.MONTH) + 1;
                int year = cal.get(Calendar.YEAR);
                if (badges.size() > 0){
                    boolean flag = false;
                    for (int j = 0 ; j < badges.size() ; j++){
                        String string = sdf.parse(badges.get(j).getScheduleList().get(0).getSch_date()).toString();
                        if (mDate.toString().equals(string)){
                            badges.get(j).getScheduleList().add(scheduleArrayList.get(i));
                            flag = true;
                            break;
                        }
                    }
                    if (!flag){
                        subSchedule.add(scheduleArrayList.get(i));
                        Badge badge = new Badge(subSchedule, day, month, year, Model.getInstance().isFlag);
                        badges.add(badge);
                    }
                }else{
                    subSchedule.add(scheduleArrayList.get(i));
                    Badge badge = new Badge(subSchedule, day, month, year, Model.getInstance().isFlag);
                    badges.add(badge);
                }

            }catch (ParseException e){
                e.printStackTrace();

            }
        }
        return badges;
    }

    public String checkDigit (int number) {
        return number <= 9 ? "0" + number : String.valueOf(number);
    }

    public void showScheduleMenu(View v, CalenderDate date, final Schedule schedule) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.done_item:
                        //Toast.makeText(ScheduleActivity.this, "Done", Toast.LENGTH_SHORT).show();
                        if (schedule.getDone_date().isEmpty()){
                            new AlertDialog.Builder(ScheduleActivity.this)
                                    .setTitle("Alert")
                                    .setMessage("Are you sure you want to be done?")
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            final ScheduleDatabase scheduleDatabase = new ScheduleDatabase(ScheduleActivity.this);
                                            db = scheduleDatabase.getWritableDatabase();
                                            Cursor res = db.rawQuery("select * from " + ScheduleDatabase.TABLE_SCHEDULE_NAME + " where "
                                                    + ScheduleDatabase.SCHEDULE_SCH_DATE + " = '" + schedule.getSch_date() + "' and "
                                                    + ScheduleDatabase.SCHEDULE_CLIENT_NAME + " = '" + schedule.getClient_name() +"'", null);
                                            if (res.getCount() > 0){
                                                DateFormat dfTo = new SimpleDateFormat("MM-dd-yyyy");
                                                String outputDate = dfTo.format(Calendar.getInstance().getTime());
                                                db.execSQL("UPDATE " + ScheduleDatabase.TABLE_SCHEDULE_NAME + " SET " + ScheduleDatabase.SCHEDULE_DONE_DATE + " = '" +  outputDate+ "' " +
                                                        " WHERE " + ScheduleDatabase.SCHEDULE_SCH_DATE + " = '" + schedule.getSch_date() + "' and "
                                                        + ScheduleDatabase.SCHEDULE_CLIENT_NAME + " = '" + schedule.getClient_name()  + "' ;" );
                                                doneSchedule(schedule.get_id());
                                                startActivity(getIntent());
                                                finish();
                                            }
                                        }
                                    }).create().show();
                        }else{
                            Toast.makeText(ScheduleActivity.this, "The schedule have already done.", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    case R.id.edit_item:

                            int scheduleIndex = 0;
                            for (int i = 0 ; i < scheduleArrayList.size() ; i++){
                                if (scheduleArrayList.get(i).equals(schedule)){
                                    scheduleIndex = i;
                                }
                            }
                            Intent intent = new Intent(ScheduleActivity.this, AddScheduleActivity.class);
                            intent.putExtra("ISEDIT",true);
                            if (!schedule.getDone_date().isEmpty()){
                                intent.putExtra("ISDONE",true);
                                intent.putExtra("DONE_DATE",schedule.getDone_date());
                            }
                            else intent.putExtra("ISDONE", false);
                            intent.putExtra("INDEX", scheduleIndex);
                            Model.getInstance().hasEditedSchedule = schedule;
                            startActivity(intent);
                            finish();
                        return false;
                    case R.id.deleted_item:
                            new AlertDialog.Builder(ScheduleActivity.this)
                                    .setTitle("Alert")
                                    .setMessage("Are you sure you want to delete?")
                                    .setNegativeButton(android.R.string.no, null)
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            final ScheduleDatabase scheduleDatabase = new ScheduleDatabase(ScheduleActivity.this);
                                            db = scheduleDatabase.getWritableDatabase();
                                            Cursor res = db.rawQuery("select * from " + ScheduleDatabase.TABLE_SCHEDULE_NAME + " where "
                                                    + ScheduleDatabase.SCHEDULE_SCH_DATE + " = '" + schedule.getSch_date() + "' and "
                                                    + ScheduleDatabase.SCHEDULE_CLIENT_NAME + " = '" + schedule.getClient_name() +"'", null);
                                            if (res.getCount() > 0){
                                                db.delete(ScheduleDatabase.TABLE_SCHEDULE_NAME, ScheduleDatabase.SCHEDULE_SCH_DATE + " = '" + schedule.getSch_date() + "' and "
                                                        + ScheduleDatabase.SCHEDULE_CLIENT_NAME + " = '" + schedule.getClient_name()  + "'",null);
                                                finish();
                                                startActivity(getIntent());
                                            }
                                        }
                                    }).create().show();
                        return false;
                    default:
                        return false;
                }
            }
        });
        popup.inflate(R.menu.schedule_menu);
        popup.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(ScheduleActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.schedule, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            Model.getInstance().displayMode = "1";
        } else if (id == R.id.nav_week) {
            Model.getInstance().displayMode = "2";
        } else if (id == R.id.nav_month) {
            Model.getInstance().displayMode = "3";
        } else if (id == R.id.nav_download){
            onExport();
        } else if (id == R.id.nav_upload){
            Intent intent = new Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
        }
        StateSaveActivity.writeMode(ScheduleActivity.this, Model.getInstance().displayMode);
        displayMode();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onExport(){
        // Export SQLite DB as EXCEL FILE
        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(getApplicationContext(), ScheduleDatabase.DB_NAME, Model.getInstance().getFilePath());
        sqliteToExcel.exportAllTables("schedule.xls", new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                Toast.makeText(ScheduleActivity.this, filePath, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123 && resultCode==RESULT_OK) {
            String filePath = getRealPathFromURI(data.getData()); //The uri with the location of the file
            onImport(filePath);
        }
    }

    public String getRealPathFromURI(Uri contentUri)
    {
        String[] proj = { MediaStore.Audio.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void onImport(String filepath){

        File file = new File(filepath);
        if (!file.exists()) {
            Toast.makeText(this, "No File", Toast.LENGTH_SHORT).show();
            return;
        }

        final ScheduleDatabase scheduleDatabase = new ScheduleDatabase(ScheduleActivity.this);
        db = scheduleDatabase.getWritableDatabase();
        // if you want to add column in excel and import into DB, you must drop the table
        ExcelToSQLite excelToSQLite = new ExcelToSQLite(getApplicationContext(), ScheduleDatabase.DB_NAME, false);
        // Import EXCEL FILE to SQLite
        excelToSQLite.importFromFile(filepath, new ExcelToSQLite.ImportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String dbName) {
                Toast.makeText(ScheduleActivity.this, "Success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ScheduleActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        db.close();
    }



}
