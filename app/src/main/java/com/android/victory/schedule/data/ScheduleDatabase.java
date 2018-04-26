package com.android.victory.schedule.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ScheduleDatabase extends SQLiteOpenHelper {

    //-- Constants ---------------------------------------------------------------------------------
    private static final String DEBUG_TAG = "Schedule_Database";
    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "Schedule_database";

    //--SCHEDULE------------------------------------------------------------------------------------
    public static final String TABLE_SCHEDULE_NAME = "Schedule";
    public static final String SCHEDULE_ID = "_id";
    public static final String SCHEDULE_ROW_ID = "schedule_row_id";
    public static final String SCHEDULE_COMP = "schedule_comp";
    public static final String SCHEDULE_SAL_NAME = "schedule_sal_name";
    public static final String SCHEDULE_SAL_ID = "schedule_sal_id";
    public static final String SCHEDULE_CLIENT_NAME = "schedule_client_name";
    public static final String SCHEDULE_CLIENT_ID = "schedule_client_id";
    public static final String SCHEDULE_JOB_NAME = "schedule_job_name";
    public static final String SCHEDULE_JOB_ID = "schedule_job_id";
    public static final String SCHEDULE_SRV_NAME = "schedule_srv_name";
    public static final String SCHEDULE_SRV_ID = "schedule_srv_id";
    public static final String SCHEDULE_SCH_DATE = "sch_date";
    public static final String SCHEDULE_MEET_NAME = "meet_name";
    public static final String SCHEDULE_KET = "schedule_ket";
    public static final String SCHEDULE_DONE_DATE = "schedule_done_date";
    public static final String SCHEDULE_SYNC_DATE = "schedule_sync_date";


    //-- Create ScheduleDatabase.
    private static final String DB_SCHEDULE_TABLE_CREATE =
            "CREATE TABLE " + TABLE_SCHEDULE_NAME +"(" +
                    SCHEDULE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SCHEDULE_ROW_ID + " TEXT," +
                    SCHEDULE_COMP + " TEXT," +
                    SCHEDULE_SAL_NAME + " TEXT," +
                    SCHEDULE_SAL_ID + " TEXT," +
                    SCHEDULE_CLIENT_NAME + " TEXT," +
                    SCHEDULE_CLIENT_ID + " TEXT," +
                    SCHEDULE_JOB_NAME + " TEXT," +
                    SCHEDULE_JOB_ID + " TEXT," +
                    SCHEDULE_SRV_NAME + " TEXT," +
                    SCHEDULE_SRV_ID + " TEXT," +
                    SCHEDULE_SCH_DATE + " TEXT," +
                    SCHEDULE_MEET_NAME + " TEXT," +
                    SCHEDULE_KET + " TEXT," +
                    SCHEDULE_DONE_DATE + " TEXT," +
                    SCHEDULE_SYNC_DATE + " TEXT);";


    public ScheduleDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(DEBUG_TAG, "Db_SCHEDULE_TABLE : " + DB_SCHEDULE_TABLE_CREATE);
        db.execSQL(DB_SCHEDULE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
