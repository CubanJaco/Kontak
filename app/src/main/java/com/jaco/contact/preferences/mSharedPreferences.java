package com.jaco.contact.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jaco.contact.R;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * Created by osvel on 7/22/16.
 */
public class mSharedPreferences {

    public static final SimpleDateFormat df = new SimpleDateFormat("D d M yyyy");

    public static String getCallPrefix(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        //get prefix
        return settings.getString(context.getResources().getString(R.string.preference_free_call_prefix), "*99");

    }

    public static String getDatabasePath(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        //get source
        return settings.getString(context.getResources().getString(R.string.preference_selected_db), "");

    }

    public static void setDatabasePath(Context context, String databasePath){
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(context.getResources().getString(R.string.preference_selected_db), databasePath);
        edit.apply();

        edit.putString(context.getResources().getString(R.string.preference_selected_db_path), databasePath);
        edit.apply();
    }

    public static int getAlertPosition(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String alertPosition = settings.getString(context.getResources().getString(R.string.preference_show_alert_position), "1");
        return Integer.parseInt(alertPosition);

    }

    public static int getShowAlert(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String showAlert = settings.getString(context.getResources().getString(R.string.preference_show_alert), "0");
        return Integer.parseInt(showAlert);

    }

    public static void setShowAlert(Context context, int alert){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(context.getResources().getString(R.string.preference_show_alert), ""+alert);
        edit.apply();

    }

    public static int getFlashAlert(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String showAlert = settings.getString(context.getResources().getString(R.string.preference_flash_notification), "3");
        return Integer.parseInt(showAlert);

    }

    public static int getSmsNotification(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String smsNotification = settings.getString(context.getResources().getString(R.string.preference_sms_notifications), "0");
        return Integer.parseInt(smsNotification);

    }

    public static int getMissedCallNotification(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String missedCallNotification = settings.getString(context.getResources().getString(R.string.preference_missed_call_notification), "0");
        return Integer.parseInt(missedCallNotification);

    }

    public static void setAlternativeDatabase(Context context, boolean alternative){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean(context.getResources().getString(R.string.preference_alternative_database), alternative);
        edit.apply();

    }

    public static boolean isAlternativeDatabase(Context context){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getResources().getString(R.string.preference_alternative_database), false);

    }

    public static boolean showBirthdayNotification(Context context){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getResources().getString(R.string.preference_birthday_notification), true);

    }

    public static int getBirthdayNotificationDialog(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String birthday_notification = settings.getString(context.getResources().getString(R.string.preference_birthday_notification_dialog), "0");
        return Integer.parseInt(birthday_notification);

    }

    public static void setNotificationToday(Context context, GregorianCalendar calendar){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(context.getResources().getString(R.string.preference_notification_today), df.format(calendar.getTime()));
        edit.apply();

    }

    public static boolean getNotificationToday(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String date = settings.getString(context.getResources().getString(R.string.preference_notification_today), "");
        return date.equals(df.format(new GregorianCalendar().getTime()));

    }

    public static int getQueryLimit(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String showAlert = settings.getString(context.getResources().getString(R.string.preference_query_limit), "20");
        return Integer.parseInt(showAlert);

    }

    public static boolean showPermissionsAlert(Context context){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getResources().getString(R.string.preference_show_permissions_alert), true);

    }

    public static boolean getIgnoreFirstIdle(Context context){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getResources().getString(R.string.preference_ignore_first_idle), false);

    }

    public static void setPermissionsAlert(Context context, boolean show){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean(context.getResources().getString(R.string.preference_show_permissions_alert), show);
        edit.apply();

    }

    public static int getFlashOnTime(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(context.getResources().getString(R.string.preference_flash_speed_on), 25) + 25;

    }

    public static void setFlashOnTime(Context context, int on_time){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putInt(context.getResources().getString(R.string.preference_flash_speed_on), on_time >= 25 ? on_time - 25 : on_time);
        edit.apply();

    }

    public static int getFlashOffTime(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getInt(context.getResources().getString(R.string.preference_flash_speed_off), 25) + 25;

    }

    public static void setFlashOffTime(Context context, int off_time){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putInt(context.getResources().getString(R.string.preference_flash_speed_off), off_time >= 25 ? off_time - 25 : off_time);
        edit.apply();

    }

    public static boolean getCorporate(Context context){

        //get shared preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getResources().getString(R.string.preference_corporate), false);

    }

    public static String getCreditConsult(Context context){

        if (getCorporate(context)){
            return "*111#";
        }

        return "*222#";

    }

    public static void setFirstTimeOpen(Context context, boolean alternative){

        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putBoolean(context.getResources().getString(R.string.preference_first_time_open), alternative);
        edit.apply();

    }

    public static boolean isFirstTimeOpen(Context context){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(context.getResources().getString(R.string.preference_first_time_open), true);

    }
}
