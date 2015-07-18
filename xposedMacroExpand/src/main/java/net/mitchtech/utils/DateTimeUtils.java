package net.mitchtech.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTimeUtils {

    private static final String TAG = AppUtils.class.getSimpleName();

    public static String getCurrentTime() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault());
        String time = simpleDateFormat.format(now.getTime());
        Log.i(TAG, time);
        return time;
    }

    public static String getDate() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String date = simpleDateFormat.format(now.getTime());
        Log.i(TAG, date);
        return date;
    }

    public static String getDayOfWeek() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String day = simpleDateFormat.format(now.getTime());
        Log.i(TAG, day);
        return day;
    }
}
