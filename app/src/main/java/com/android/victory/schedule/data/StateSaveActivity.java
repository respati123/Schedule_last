package com.android.victory.schedule.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class StateSaveActivity {

    //-- First time --------------------------------------------------------------------------------
    /**
     * Read the preference PREF_FIRST_TIME.
     */
    public static boolean readFirstTime(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.PREF_FIRST_TIME, Context.MODE_PRIVATE);
        return sp.getBoolean(Constants.PREF_FIRST_TIME, true);
    }

    /**
     * Write the preference PREF_FIRST_TIME.
     */
    public static void writeFirstTime(Context oContext, boolean oValue){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.PREF_FIRST_TIME, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean(Constants.PREF_FIRST_TIME, oValue);
        spe.apply();
    }

    //-- Password ----------------------------------------------------------------------------------
    /**
     * Write the preference PASSWORD.
     */
    public static void writePassword(Context oContext, String oPass){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.PASSWORD, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(Constants.PASSWORD, oPass);
        spe.apply();
    }

    /**
     * Read the preference PREF_PASSWORD.
     */
    public static String readPassword(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.PASSWORD, Context.MODE_PRIVATE);
        return sp.getString(Constants.PASSWORD, "");
    }

    //-- UserName ----------------------------------------------------------------------------------
    /**
     * Write the preference USERNAME.
     */
    public static void writeUserName(Context oContext, String oPass){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.USERNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(Constants.USERNAME, oPass);
        spe.apply();
    }

    /**
     * Read the preference PREF_USERNAME.
     */
    public static String readUserName(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.USERNAME, Context.MODE_PRIVATE);
        return sp.getString(Constants.USERNAME, "");
    }

    //-- Comp ----------------------------------------------------------------------------------
    /**
     * Write the preference COMP.
     */
    public static void writeComp(Context oContext, String oPass){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.COMP, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(Constants.COMP, oPass);
        spe.apply();
    }

    /**
     * Read the preference COMP.
     */
    public static String readComp(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.COMP, Context.MODE_PRIVATE);
        return sp.getString(Constants.COMP, "");
    }

    //-- Photo ----------------------------------------------------------------------------------
    /**
     * Write the preference Photo.
     */
    public static void writePhoto(Context oContext, Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encoded = Base64.encodeToString(b, Base64.DEFAULT);
        SharedPreferences sp = oContext.getSharedPreferences(Constants.PHOTO, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(Constants.COMP, encoded);
        spe.apply();
    }

    /**
     * Read the preference Photo.
     */
    public static Bitmap readPhoto(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.PHOTO, Context.MODE_PRIVATE);
        String encoded = sp.getString(Constants.COMP, "");
        byte[] imageAsBytes = Base64.decode(encoded,0);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    //-- MODE ----------------------------------------------------------------------------------
    /**
     * Write the preference MODE.
     */
    public static void writeMode(Context oContext, String oPass){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.MODE, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(Constants.MODE, oPass);
        spe.apply();
    }

    /**
     * Read the preference MODE.
     */
    public static String readMode(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.MODE, Context.MODE_PRIVATE);
        return sp.getString(Constants.MODE, "");
    }

    //-- First Time ----------------------------------------------------------------------------------
    /**
     * Write the preference First Time.
     */
    public static void writeTime(Context oContext, float oPass){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.CURRENTTIME, Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putFloat(Constants.CURRENTTIME, oPass);
        spe.apply();
    }

    /**
     * Read the preference MODE.
     */
    public static float readTime(Context oContext){
        SharedPreferences sp = oContext.getSharedPreferences(Constants.CURRENTTIME, Context.MODE_PRIVATE);
        return sp.getFloat(Constants.CURRENTTIME, 0);
    }

}
