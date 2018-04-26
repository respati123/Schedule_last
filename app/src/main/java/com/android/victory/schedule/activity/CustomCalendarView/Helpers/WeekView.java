package com.android.victory.schedule.activity.CustomCalendarView.Helpers;

import com.android.victory.schedule.data.Schedule;

import java.util.Calendar;

/**
 * Created by ThinDV on 6/19/2017.
 */

public interface WeekView {
    interface OnDaySelected{
        public void onSelected(Schedule schedule);
    }
}
