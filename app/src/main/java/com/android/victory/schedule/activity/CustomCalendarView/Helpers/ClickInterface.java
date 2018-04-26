package com.android.victory.schedule.activity.CustomCalendarView.Helpers;


import android.icu.util.Calendar;

import com.android.victory.schedule.data.Schedule;

public interface ClickInterface {

    /*
    * called when clicked on date in calender view
    * */

    void setDateClicked(CalenderDate date, Schedule schedule);

}
