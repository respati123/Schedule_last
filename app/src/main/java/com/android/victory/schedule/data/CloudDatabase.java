package com.android.victory.schedule.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CloudDatabase extends SQLiteOpenHelper {

    //-- Constants ---------------------------------------------------------------------------------
    private static final String DEBUG_TAG = "Cloud_Database";
    private static final int DB_VERSION = 1;
    public static final String DB_NAME = "Cloud_database";

    //--Client--------------------------------------------------------------------------------------
    public static final String TABLE_CLIENT_NAME = "Client";
    public static final String CLIENT_ID = "_id";
    public static final String CLIENT_ROW_ID = "client_row_id";
    public static final String CLIENT_CODE = "client_code";
    public static final String CLIENT_NAME = "client_name";
    public static final String CLIENT_ADDRESS = "client_addr";
    public static final String CLIENT_PHONE = "client_phone";
    public static final String CLIENT_HP = "client_hp";
    public static final String CLIENT_GEO_LATT = "client_geo_latt";
    public static final String CLIENT_GEO_LONG = "client_geo_long";
    public static final String CLIENT_KET = "client_ket";
    public static final String CLIENT_SYNC_DATE = "client_sync_date";
    public static final String CLIENT_UPDATE_USER = "client_update_user";
    public static final String CLIENT_UPDATE_DATE = "client_update_date";
    //--Job-----------------------------------------------------------------------------------------
    public static final String TABLE_JOB_NAME = "Job";
    public static final String JOB_ID = "_id";
    public static final String JOB_ROW_ID = "job_row_id";
    public static final String JOB_CODE = "job_code";
    public static final String JOB_NAME = "job_name";
    public static final String JOB_KET = "job_ket";
    public static final String JOB_UPDATE_USER = "job_update_user";
    public static final String JOB_UPDATE_DATE = "job_update_date";
    //--SalesMan------------------------------------------------------------------------------------
    public static final String TABLE_SALESMAN_NAME = "SalesMan";
    public static final String SALESMAN_ID = "_id";
    public static final String SALESMAN_ROW_ID = "salesman_row_id";
    public static final String SALESMAN_CODE = "salesman_code";
    public static final String SALESMAN_NAME = "salesman_name";
    public static final String SALESMAN_PICT_DATA = "salesman_pict_data";
    public static final String SALESMAN_SELF_FLAG = "salesman_self_flag";
    public static final String SALESMAN_ADDRESS = "salesman_addr";
    public static final String SALESMAN_PHONE = "salesman_phone";
    public static final String SALESMAN_HP = "salesman_hp";
    public static final String SALESMAN_GEO_LATT = "salesman_geo_latt";
    public static final String SALESMAN_GEO_LONG = "salesman_geo_long";
    public static final String SALESMAN_SYNC_DATE = "salesman_SYNC_DATE";
    public static final String SALESMAN_SYNC_FLAG = "salesman_SYNC_FLAG";
    public static final String SALESMAN_KET = "salesman_ket";
    public static final String SALESMAN_UPDATE_USER = "salesman_update_user";
    public static final String SALESMAN_UPDATE_DATE = "salesman_update_date";
    //--Srv-------------------------------------------------------------------------------------
    public static final String TABLE_SERVICE_NAME = "Srv";
    public static final String SERVICE_ID = "_id";
    public static final String SERVICE_ROW_ID = "srv_row_id";
    public static final String SERVICE_CODE = "srv_code";
    public static final String SERVICE_NAME = "srv_name";
    public static final String SERVICE_KET = "srv_ket";
    public static final String SERVICE_UPDATE_USER = "srv_update_user";
    public static final String SERVICE_UPDATE_DATE = "srv_update_date";

    //-- Create ClientTable.
    private static final String DB_CLIENT_TABLE_CREATE =
            "CREATE TABLE " + TABLE_CLIENT_NAME +"(" +
                    CLIENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CLIENT_ROW_ID + " TEXT," +
                    CLIENT_CODE + " TEXT," +
                    CLIENT_NAME + " TEXT," +
                    CLIENT_ADDRESS + " TEXT," +
                    CLIENT_PHONE + " TEXT," +
                    CLIENT_HP + " TEXT," +
                    CLIENT_GEO_LATT + " TEXT," +
                    CLIENT_GEO_LONG + " TEXT," +
                    CLIENT_KET + " TEXT," +
                    CLIENT_SYNC_DATE + " TEXT," +
                    CLIENT_UPDATE_USER + " TEXT," +
                    CLIENT_UPDATE_DATE + " TEXT);";
    //-- Create JobTable.
    private static final String DB_JOB_TABLE_CREATE =
            "CREATE TABLE " + TABLE_JOB_NAME +"(" +
                    JOB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    JOB_ROW_ID + " TEXT," +
                    JOB_CODE + " TEXT," +
                    JOB_NAME + " TEXT," +
                    JOB_KET + " TEXT," +
                    JOB_UPDATE_USER + " TEXT," +
                    JOB_UPDATE_DATE + " TEXT);";
    //-- Create SalesmanTable.
    private static final String DB_SALESMAN_TABLE_CREATE =
            "CREATE TABLE " + TABLE_SALESMAN_NAME +"(" +
                    SALESMAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SALESMAN_ROW_ID + " TEXT," +
                    SALESMAN_CODE + " TEXT," +
                    SALESMAN_NAME + " TEXT," +
                    SALESMAN_PICT_DATA + " TEXT," +
                    SALESMAN_SELF_FLAG + " TEXT," +
                    SALESMAN_ADDRESS + " TEXT," +
                    SALESMAN_PHONE + " TEXT," +
                    SALESMAN_HP + " TEXT," +
                    SALESMAN_GEO_LATT + " TEXT," +
                    SALESMAN_GEO_LONG + " TEXT," +
                    SALESMAN_SYNC_DATE + " TEXT," +
                    SALESMAN_SYNC_FLAG + " TEXT," +
                    SALESMAN_KET + " TEXT," +
                    SALESMAN_UPDATE_USER + " TEXT," +
                    SALESMAN_UPDATE_DATE + " TEXT);";
    //-- Create ServiceTable.
    private static final String DB_SERVICE_TABLE_CREATE =
            "CREATE TABLE " + TABLE_SERVICE_NAME +"(" +
                    SERVICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SERVICE_ROW_ID + " TEXT," +
                    SERVICE_CODE + " TEXT," +
                    SERVICE_NAME + " TEXT," +
                    SERVICE_KET + " TEXT," +
                    SERVICE_UPDATE_USER + " TEXT," +
                    SERVICE_UPDATE_DATE + " TEXT);";


    public CloudDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(DEBUG_TAG, "Db_CLIENT_TABLE : " + DB_CLIENT_TABLE_CREATE);
        db.execSQL(DB_CLIENT_TABLE_CREATE);
        Log.d(DEBUG_TAG, "Db_JOB_TABLE : " + DB_JOB_TABLE_CREATE);
        db.execSQL(DB_JOB_TABLE_CREATE);
        Log.d(DEBUG_TAG, "Db_SALESMAN_TABLE : " + DB_SALESMAN_TABLE_CREATE);
        db.execSQL(DB_SALESMAN_TABLE_CREATE);
        Log.d(DEBUG_TAG, "Db_SERVICE_TABLE : " + DB_SERVICE_TABLE_CREATE);
        db.execSQL(DB_SERVICE_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
