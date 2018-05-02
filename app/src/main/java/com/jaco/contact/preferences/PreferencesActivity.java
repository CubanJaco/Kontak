package com.jaco.contact.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;

import com.jaco.contact.Birthday;
import com.jaco.contact.BirthdayDatabase;
import com.jaco.contact.R;
import com.jaco.contact.fileChooser.FileChooserActivity;

import java.util.List;

/**
 * Created by osvel on 8/2/16.
 */
public class PreferencesActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    public static final int FILE_CHOOSER_RESULT = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference birthday_notifications = findPreference(getResources().getString(R.string.preference_birthday_notification_dialog));
        birthday_notifications.setOnPreferenceChangeListener(this);

        final Preference pref = findPreference(getResources().getString(R.string.preference_selected_db));
        pref.setPersistent(true);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                //get shared preferences
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                //get source
                String path = settings.getString(getResources().getString(R.string.preference_selected_db), "");

                Intent intent = new Intent(PreferencesActivity.this, FileChooserActivity.class);
                intent.putExtra(FileChooserActivity.FILE_PATH, path);
                startActivityForResult(intent, FILE_CHOOSER_RESULT);
                return true;
            }
        });

        Preference alert_pref = findPreference(getResources().getString(R.string.preference_flash_speed));
        alert_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                buildFlashAlertDialog();
                return true;
            }
        });

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (getResources().getString(R.string.preference_birthday_notification_dialog).equals(key)){
            int days_before = Integer.parseInt((String)newValue);
            new mAsyncTask(this).execute(days_before);
            return true;
        }
        return false;
    }

    public void buildFlashAlertDialog(){

        AlertDialog.Builder flash_alerts = new AlertDialog.Builder(this);
        final View rootView_flash_alert = getLayoutInflater().inflate(R.layout.flash_speed_setting, null);
        flash_alerts.setView(rootView_flash_alert);
        flash_alerts.setTitle(R.string.flash_time);

        final SeekBar on_time = (SeekBar) rootView_flash_alert.findViewById(R.id.on_time);
        final SeekBar off_time = (SeekBar) rootView_flash_alert.findViewById(R.id.off_time);

        on_time.setProgress(mSharedPreferences.getFlashOnTime(this));
        off_time.setProgress(mSharedPreferences.getFlashOffTime(this));

        flash_alerts.setNegativeButton(R.string.cancel, null);
        flash_alerts.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mSharedPreferences.setFlashOnTime(PreferencesActivity.this, on_time.getProgress());
                mSharedPreferences.setFlashOffTime(PreferencesActivity.this, off_time.getProgress());

            }
        });

        flash_alerts.show();

    }

    public class mAsyncTask extends AsyncTask<Integer, Void, Void> {

        private Context context;

        public mAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            BirthdayDatabase database = new BirthdayDatabase(context);
            List<Birthday> birthdays = database.getAllBirthdays(true);
            for (Birthday bd : birthdays)
                bd.setSchedule(context, integers[0]);

            return null;
        }
    }

}
