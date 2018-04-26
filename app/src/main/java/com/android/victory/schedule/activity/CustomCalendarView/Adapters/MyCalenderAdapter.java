package com.android.victory.schedule.activity.CustomCalendarView.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.android.victory.schedule.R;
import com.android.victory.schedule.activity.CustomCalendarView.CustomCalendar;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.Badge;
import com.android.victory.schedule.activity.CustomCalendarView.Helpers.CalenderDate;
import com.android.victory.schedule.data.Model;
import com.android.victory.schedule.data.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MyCalenderAdapter extends RecyclerView.Adapter<MyCalenderAdapter.ViewHolder> {

    Context context;
    int maxDay, firstDay, month, year, tDay;
    String[] day = {"Sun", "Mon", "Thu", "Wen", "Thr", "Fri", "Sat"};

    int cellWidthSize = 50;
    int cellHeightSize = 50;

    List<Badge> badges;
    ListAdapter listAdapter;

    public MyCalenderAdapter(Context context, int maxDay, int firstDay, int month, int year, int tDay, int cellWidthSize,int cellHeightSize, List<Badge> badges) {

        this.context = context;
        this.maxDay = maxDay;
        this.firstDay = firstDay;
        this.month = month;
        this.year = year;
        this.tDay = tDay;
        this.cellWidthSize = cellWidthSize;
        this.cellHeightSize = cellHeightSize;
        this.badges = badges;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_calender_cell, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        if (position % 7 == 0)
            holder.tvDate.setTextColor(Color.RED);

        if (position < 7)
            holder.tvDate.setText("" + day[position]);
        else if (position > (7 + firstDay - 2)) {
            int toDay = (position - (7 + firstDay - 2));
            holder.tvDate.setText("" + toDay);
            for(int i=0;i<badges.size();i++){
                if(toDay == badges.get(i).getDay()){
                    listAdapter = new ListAdapter(this.context,badges.get(i).getScheduleList(),badges.get(i).getDone());
                    holder.tvBadgeListView.setAdapter(listAdapter);
                }
            }
        }

        holder.tvBadgeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                if (position > (7 + firstDay - 2)) {
                    int toDay = (position - (7 + firstDay - 2));
                    CalenderDate date = new CalenderDate();

                    date.setDay(toDay);
                    date.setMonth(month + 1);
                    date.setYear(year);
                    for(int i=0;i<badges.size();i++){
                        if(toDay == badges.get(i).getDay()){
                            Schedule schedule = badges.get(i).getScheduleList().get(index);
                            if (CustomCalendar.staticClickInterface() != null && !badges.get(i).getDone())
                                CustomCalendar.staticClickInterface().setDateClicked(date,schedule);
                        }
                    }
                }
            }
        });

    }




    @Override
    public int getItemCount() {
        return maxDay + 7 + firstDay - 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDate;
        private ListView tvBadgeListView;
        private LinearLayout llCell;

        private ViewHolder(View view) {
            super(view);

            tvDate = (TextView) view.findViewById(R.id.tvDate);       //for date of month
            tvBadgeListView = (ListView) view.findViewById(R.id.client_rowid_listview);     //for badge on date
            llCell = (LinearLayout) view.findViewById(R.id.llCell);   //single date layout
            llCell.setLayoutParams(new LinearLayout.LayoutParams(cellWidthSize, cellHeightSize)); //adjusting width and height for single date layout
        }
    }

    public class ListAdapter extends BaseAdapter{

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

