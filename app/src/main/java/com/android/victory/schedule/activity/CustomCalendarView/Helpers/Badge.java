package com.android.victory.schedule.activity.CustomCalendarView.Helpers;


import com.android.victory.schedule.data.Schedule;

import java.util.ArrayList;

public class Badge {

    int day,month, year;
    boolean flag;
    ArrayList<Schedule> ScheduleList;

    public Badge(){

    }

    public Badge( ArrayList<Schedule> ScheduleList,int day,int month, int year, boolean flag){
        this.ScheduleList = ScheduleList;
        this.day = day;
        this.month = month;
        this.year = year;
        this.flag = flag;
    }

    public ArrayList<Schedule> getScheduleList() {
        return ScheduleList;
    }

    public void setClientSchedule(ArrayList<Schedule> ScheduleList) {
        this.ScheduleList = ScheduleList;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear(){return year;}

    public void setYear(int year){this.year = year;}

    public boolean getDone(){return flag;}

    public void setDone(boolean isDone){this.flag = flag;}
}
