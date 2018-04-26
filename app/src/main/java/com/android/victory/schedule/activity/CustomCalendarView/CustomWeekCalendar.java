package com.android.victory.schedule.activity.CustomCalendarView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.Image;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.victory.schedule.R;
import com.android.victory.schedule.activity.CustomCalendarView.Adapters.MyCalenderAdapter;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.Badge;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.ClickInterface;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.Utilities;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.WeekView;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomWeekCalendar extends LinearLayout implements View.OnClickListener{

    public static final String FORMAT_DATE_EEEE_MMMM_DD_YYYY = "MMMM yyyy";

    private String colorOfSelectedDate;

    private Context context;
    private View rootView;
    private SimpleDateFormat sdf;
    private Calendar inWeekCalendar;
    private Calendar selectedCalendar;
    private StateListDrawable dateDrawable;

    private TextView tvDateDetails;
    private ImageView linPreviousWeek;
    private ImageView linNextWeek;

    private TextView tvSundayDate;
    private TextView tvMondayDate;
    private TextView tvTuesdayDate;
    private TextView tvWednesdayDate;
    private TextView tvThursdayDate;
    private TextView tvFridayDate;
    private TextView tvSaturdayDate;

    private ListView clientMondayListView;
    private ListView clientTuesdayListView;
    private ListView clientWednesdayListView;
    private ListView clientThursdayListView;
    private ListView clientFridayListView;
    private ListView clientSaturdayListView;
    private ListView clientSundayListView;

    private WeekView.OnDaySelected onDateSelected;

    private ArrayList<Schedule> totalScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> badgsScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> mondayScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> tuesdayScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> wednesdayScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> thursdayScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> fridayScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> saturdayScheduleArrayList = new ArrayList<>();
    private ArrayList<Schedule> sundayScheduleArrayList = new ArrayList<>();


    public CustomWeekCalendar(Context context) {
        super(context);
        this.context = context;
        initViews();
    }

    public CustomWeekCalendar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomCalendar);
        final int length = typedArray.getIndexCount();
        for (int i=0; i < length; i++){
            int attr = typedArray.getIndex(i);
            if(attr == R.styleable.CustomCalendar_cellSize){
                colorOfSelectedDate = typedArray.getString(attr);
            }
        }
        typedArray.recycle();

        initVariables();
        initViews();
    }

    private void initVariables() {
        int color = Utilities.getThemeColor(context, R.attr.colorAccent);
        dateDrawable = new StateListDrawable();

        ShapeDrawable defaultShape = new ShapeDrawable(new OvalShape());
        defaultShape.getPaint().setColor(Color.TRANSPARENT);
        dateDrawable.addState(new int[]{}, defaultShape);

        ShapeDrawable selectedShape = new ShapeDrawable(new OvalShape());
        selectedShape.getPaint().setColor(Color.RED);
        dateDrawable.addState(new int[]{android.R.attr.state_selected}, selectedShape);

        ShapeDrawable pressShape = new ShapeDrawable(new OvalShape());
        pressShape.getPaint().setColor(Color.GRAY);
        dateDrawable.addState(new int[]{android.R.attr.state_pressed}, pressShape);

    }

    private void initViews() {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = layoutInflater.inflate(R.layout.week_view, this);

        tvDateDetails = (TextView) rootView.findViewById(R.id.tvDateDetails);
        linPreviousWeek = (ImageView) rootView.findViewById(R.id.linPreviousWeek);
        linPreviousWeek.setOnClickListener(this);
        linNextWeek = (ImageView) rootView.findViewById(R.id.linNextWeek);
        linNextWeek.setOnClickListener(this);

        tvSundayDate = (TextView) findViewById(R.id.tvSundayDate);
        tvSundayDate.setOnClickListener(this);
        clientSundayListView = (ListView)findViewById(R.id.client_sunday_listview);
        tvMondayDate = (TextView) findViewById(R.id.tvMondayDate);
        tvMondayDate.setOnClickListener(this);
        clientMondayListView = (ListView)findViewById(R.id.client_monday_listview);
        tvTuesdayDate = (TextView) findViewById(R.id.tvTuesdayDate);
        tvTuesdayDate.setOnClickListener(this);
        clientTuesdayListView = (ListView)findViewById(R.id.client_tuesday_listview);
        tvWednesdayDate = (TextView) findViewById(R.id.tvWednesdayDate);
        tvWednesdayDate.setOnClickListener(this);
        clientWednesdayListView = (ListView)findViewById(R.id.client_wednesday_listview);
        tvThursdayDate = (TextView) findViewById(R.id.tvThursdayDate);
        tvThursdayDate.setOnClickListener(this);
        clientThursdayListView = (ListView)findViewById(R.id.client_thursday_listview);
        tvFridayDate = (TextView) findViewById(R.id.tvFridayDate);
        tvFridayDate.setOnClickListener(this);
        clientFridayListView = (ListView)findViewById(R.id.client_friday_listview);
        tvSaturdayDate = (TextView) findViewById(R.id.tvSaturdayDate);
        tvSaturdayDate.setOnClickListener(this);
        clientSaturdayListView = (ListView)findViewById(R.id.client_saturday_listview);

        inWeekCalendar = Calendar.getInstance();
        selectedCalendar = Calendar.getInstance();

        updateCurrentDate();

    }

    public void setTotalScheduleArrayList(ArrayList<Schedule> totalScheduleArrayList){
        this.totalScheduleArrayList = totalScheduleArrayList;
        updateCurrentDate();
    }

    public void setOnDateSelected(WeekView.OnDaySelected onDateSelected) {
        this.onDateSelected = onDateSelected;
    }

    /**
     * get current date and update to inWeekCalendar
     */
    private void updateCurrentDate() {
        try {
            String localLang = Locale.getDefault().getLanguage();
            if(localLang.equals("in")){
                sdf = new SimpleDateFormat(FORMAT_DATE_EEEE_MMMM_DD_YYYY,new Locale("in", "ID"));
            } else {
                sdf = new SimpleDateFormat(FORMAT_DATE_EEEE_MMMM_DD_YYYY, Locale.US);
            }

            String strCurrentDate = sdf.format(inWeekCalendar.getTime());
            tvDateDetails.setText(strCurrentDate);
            Calendar currentCalendar = Calendar.getInstance();
            String currentDate = new SimpleDateFormat("MM-dd-yyyy").format(inWeekCalendar.getTime());
            currentCalendar.set(Calendar.YEAR, Integer.parseInt(currentDate.split("-")[2]));
            currentCalendar.set(Calendar.MONTH, Integer.parseInt(currentDate.split("-")[0]));
            currentCalendar.set(Calendar.DATE, Integer.parseInt(currentDate.split("-")[1]));
            int week = currentCalendar.get(Calendar.WEEK_OF_MONTH);
            int month = currentCalendar.get(Calendar.MONTH);
            int year = currentCalendar.get(Calendar.YEAR);

            updateWeekCalendar(week, month, year);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void updateWeekCalendar(int week, int month, int year) {
        try {
            Calendar mondayDate = getMondayOfWeek((Calendar) inWeekCalendar.clone());
            Calendar sunDayDate = getDateNumInWeek((Calendar) mondayDate.clone(), -1);
            Calendar tuesdayDate = getDateNumInWeek((Calendar) mondayDate.clone(), 1);
            Calendar wednesdayDate = getDateNumInWeek((Calendar) mondayDate.clone(), 2);
            Calendar thursdayDate = getDateNumInWeek((Calendar) mondayDate.clone(), 3);
            Calendar fridayDate = getDateNumInWeek((Calendar) mondayDate.clone(), 4);
            Calendar saturdayDate = getDateNumInWeek((Calendar) mondayDate.clone(), 5);

            String toMondayDay = String.valueOf(mondayDate.get(Calendar.DAY_OF_MONTH));
            tvMondayDate.setText(toMondayDay);
            String toSundayDay = String.valueOf(sunDayDate.get(Calendar.DAY_OF_MONTH));
            tvSundayDate.setText(toSundayDay);
            String toTuesdayDay = String.valueOf(tuesdayDate.get(Calendar.DAY_OF_MONTH));
            tvTuesdayDate.setText(toTuesdayDay);
            String toWednesdayDay = String.valueOf(wednesdayDate.get(Calendar.DAY_OF_MONTH));
            tvWednesdayDate.setText(toWednesdayDay);
            String toThursdayDay = String.valueOf(thursdayDate.get(Calendar.DAY_OF_MONTH));
            tvThursdayDate.setText(toThursdayDay);
            String toFridayDay = String.valueOf(fridayDate.get(Calendar.DAY_OF_MONTH));
            tvFridayDate.setText(toFridayDay);
            String toSaturdayDay = String.valueOf(saturdayDate.get(Calendar.DAY_OF_MONTH));
            tvSaturdayDate.setText(toSaturdayDay);

            badgsScheduleArrayList = new ArrayList<>();
            mondayScheduleArrayList = new ArrayList<>();
            tuesdayScheduleArrayList = new ArrayList<>();
            wednesdayScheduleArrayList = new ArrayList<>();
            thursdayScheduleArrayList = new ArrayList<>();
            fridayScheduleArrayList = new ArrayList<>();
            saturdayScheduleArrayList = new ArrayList<>();
            sundayScheduleArrayList = new ArrayList<>();

            badgsScheduleArrayList = badgsWeek(week, month, year);
            for (int i = 0 ; i < badgsScheduleArrayList.size() ; i++){
                int badgsYear = Integer.parseInt(badgsScheduleArrayList.get(i).getSch_date().split("---")[0].split("-")[2]);
                int badgsMonth = Integer.parseInt(badgsScheduleArrayList.get(i).getSch_date().split("---")[0].split("-")[0]);
                if (badgsYear == year && badgsMonth == month){
                    String day = String.valueOf(Integer.parseInt(badgsScheduleArrayList.get(i).getSch_date().split("---")[0].split("-")[1]));
                    if (toMondayDay.equals(day)){
                        mondayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }else if (toTuesdayDay.equals(day)){
                        tuesdayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }else if (toWednesdayDay.equals(day)){
                        wednesdayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }else if (toThursdayDay.equals(day)){
                        thursdayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }else if (toFridayDay.equals(day)){
                        fridayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }else if (toSaturdayDay.equals(day)){
                        saturdayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }else if (toSundayDay.equals(day)){
                        sundayScheduleArrayList.add(badgsScheduleArrayList.get(i));
                    }
                }
            }

            ListAdapter mondayListAdapter = new ListAdapter(this.context,mondayScheduleArrayList, Model.getInstance().isFlag);
            clientMondayListView.setAdapter(mondayListAdapter);
            clientMondayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(mondayScheduleArrayList.get(position));
                }
            });

            ListAdapter tuesdayListAdapter = new ListAdapter(this.context,tuesdayScheduleArrayList, Model.getInstance().isFlag);
            clientTuesdayListView.setAdapter(tuesdayListAdapter);
            clientTuesdayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(tuesdayScheduleArrayList.get(position));
                }
            });

            ListAdapter wednesdayListAdpater = new ListAdapter(this.context,wednesdayScheduleArrayList, Model.getInstance().isFlag);
            clientWednesdayListView.setAdapter(wednesdayListAdpater);
            clientWednesdayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(wednesdayScheduleArrayList.get(position));
                }
            });

            ListAdapter thursdayListAdpater = new ListAdapter(this.context,thursdayScheduleArrayList, Model.getInstance().isFlag);
            clientThursdayListView.setAdapter(thursdayListAdpater);
            clientThursdayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(thursdayScheduleArrayList.get(position));
                }
            });

            ListAdapter fridayListAdpater = new ListAdapter(this.context,fridayScheduleArrayList, Model.getInstance().isFlag);
            clientFridayListView.setAdapter(fridayListAdpater);
            clientFridayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(fridayScheduleArrayList.get(position));
                }
            });

            ListAdapter saturdayListAdapter = new ListAdapter(this.context,saturdayScheduleArrayList, Model.getInstance().isFlag);
            clientSaturdayListView.setAdapter(saturdayListAdapter);
            clientSaturdayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(saturdayScheduleArrayList.get(position));
                }
            });

            ListAdapter sundayListApapter = new ListAdapter(this.context,sundayScheduleArrayList, Model.getInstance().isFlag);
            clientSundayListView.setAdapter(sundayListApapter);
            clientSundayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDateSelected.onSelected(sundayScheduleArrayList.get(position));
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<Schedule> badgsWeek(int week, int month, int year){
        ArrayList<Schedule> scheduleArrayList = new ArrayList<>();
        if (totalScheduleArrayList != null && totalScheduleArrayList.size() != 0){
            for (int i = 0 ; i < totalScheduleArrayList.size() ; i++){
                int sYear = getYear(totalScheduleArrayList.get(i));
                if (getYear(totalScheduleArrayList.get(i)) == year){
                    int sMonth = getMonth(totalScheduleArrayList.get(i));
                    if (getMonth(totalScheduleArrayList.get(i)) == month){
                        int sWeek = getWeek(totalScheduleArrayList.get(i));
                        if (getWeek(totalScheduleArrayList.get(i)) == week){
                            scheduleArrayList.add(totalScheduleArrayList.get(i));
                        }
                    }
                }
            }
            return scheduleArrayList;
        }
        return new ArrayList<>();
    }

    private int getDay(Schedule schedule){
        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.set(Calendar.YEAR, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[2]));
        scheduleCalendar.set(Calendar.MONTH, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[0]));
        scheduleCalendar.set(Calendar.DATE, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[1]));
        return scheduleCalendar.get(Calendar.DAY_OF_MONTH);
    }

    private int getWeek(Schedule schedule){
        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.set(Calendar.YEAR, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[2]));
        scheduleCalendar.set(Calendar.MONTH, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[0]));
        scheduleCalendar.set(Calendar.DATE, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[1]));
        return scheduleCalendar.get(Calendar.WEEK_OF_MONTH);
    }

    private int getMonth(Schedule schedule){
        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.set(Calendar.YEAR, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[2]));
        scheduleCalendar.set(Calendar.MONTH, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[0]));
        scheduleCalendar.set(Calendar.DATE, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[1]));
        return scheduleCalendar.get(Calendar.MONTH);
    }

    private int getYear(Schedule schedule){
        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.set(Calendar.YEAR, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[2]));
        scheduleCalendar.set(Calendar.MONTH, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[0]));
        scheduleCalendar.set(Calendar.DATE, Integer.parseInt(schedule.getSch_date().split("---")[0].split("-")[1]));
        return scheduleCalendar.get(Calendar.YEAR);
    }

    /**
     * get day_of_month in week
     * @param mondayCalendar
     * @return
     */

    private Calendar getDateNumInWeek(Calendar mondayCalendar, int i) {
        mondayCalendar.add(Calendar.DATE, i);
        return mondayCalendar;
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.linPreviousWeek) {
            inWeekCalendar.add(Calendar.DATE, -7);
            updateCurrentDate();
        } else if (i == R.id.linNextWeek){
            inWeekCalendar.add(Calendar.DATE, 7);
            updateCurrentDate();
        } else if (i == R.id.tvSundayDate){
            updateSelectedDate(Calendar.SUNDAY);
        } else if (i == R.id.tvMondayDate){
            updateSelectedDate(Calendar.MONDAY);
        } else if (i == R.id.tvTuesdayDate) {
            updateSelectedDate(Calendar.TUESDAY);
        } else if (i == R.id.tvWednesdayDate) {
            updateSelectedDate(Calendar.WEDNESDAY);
        } else if (i == R.id.tvThursdayDate) {
            updateSelectedDate(Calendar.THURSDAY);
        } else if (i == R.id.tvFridayDate) {
            updateSelectedDate(Calendar.FRIDAY);
        } else if (i == R.id.tvSaturdayDate) {
            updateSelectedDate(Calendar.SATURDAY);
        }
    }

    private void updateSelectedDate(int day) {
        Calendar mondayOfWeek = getMondayOfWeek((Calendar) inWeekCalendar.clone());
        switch (day){
            case Calendar.SUNDAY:
                mondayOfWeek.add(Calendar.DATE, -1);
                break;

            case Calendar.TUESDAY:
                mondayOfWeek.add(Calendar.DATE, 1);
                break;

            case Calendar.WEDNESDAY:
                mondayOfWeek.add(Calendar.DATE, 2);
                break;

            case Calendar.THURSDAY:
                mondayOfWeek.add(Calendar.DATE, 3);
                break;

            case Calendar.FRIDAY:
                mondayOfWeek.add(Calendar.DATE, 4);
                break;

            case Calendar.SATURDAY:
                mondayOfWeek.add(Calendar.DATE, 5);
                break;
        }

        selectedCalendar = (Calendar) mondayOfWeek.clone();
        inWeekCalendar = (Calendar) mondayOfWeek.clone();

        /*if(null != onDateSelected){
            onDateSelected.onSelected((Calendar) selectedCalendar.clone());
        }*/
        updateCurrentDate();
    }

    /**
     * get first day of week
     * @param dateInWeek
     * @return
     */
    private static Calendar getMondayOfWeek(Calendar dateInWeek) {
        int dayOfWeek = dateInWeek.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek == Calendar.SUNDAY){
            dateInWeek.add(Calendar.DATE, 1);
        } else {
            dateInWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        return dateInWeek;
    }

    public class ListAdapter extends BaseAdapter {

        Context context;
        ArrayList<Schedule> subScheduleList;
        boolean flag;
        public ListAdapter(Context context, ArrayList<Schedule> subScheduleList,boolean flag){
            //super(context, R.layout.single_list_app_item, utilsArrayList);
            this.context = context;
            this.subScheduleList = subScheduleList;
            this.flag = flag;
        }

        @Override
        public int getCount() {
            return subScheduleList.size();
        }

        @Override
        public Schedule getItem(int i) {
            return subScheduleList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public ArrayList<Schedule> getData(){
            return subScheduleList;
        }


        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.client_list_item, parent, false);
            }
            TextView clientNameTextView = (TextView)convertView.findViewById(R.id.client_row_textview);
            if (subScheduleList.get(position).getDone_date().isEmpty() && milliseconds(subScheduleList.get(position).getSch_date()) < System.currentTimeMillis()){
                clientNameTextView.setBackground(context.getResources().getDrawable(R.drawable.red_rect));
            }else if (!subScheduleList.get(position).getDone_date().isEmpty() && milliseconds(subScheduleList.get(position).getDone_date()) < System.currentTimeMillis()){
                if (!flag)clientNameTextView.setBackground(context.getResources().getDrawable(R.drawable.green_rect));
                else clientNameTextView.setVisibility(View.GONE);
            }else if (subScheduleList.get(position).getDone_date().isEmpty() && milliseconds(subScheduleList.get(position).getSch_date()) >= System.currentTimeMillis()){
                if (!flag)clientNameTextView.setBackground(context.getResources().getDrawable(R.drawable.blue_rect));
                else clientNameTextView.setVisibility(View.GONE);
            }
            clientNameTextView.setVisibility(View.VISIBLE);
            clientNameTextView.setText(subScheduleList.get(position).getClient_name());


            return convertView;
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

    }

}
