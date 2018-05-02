package com.jaco.contact.preferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.jaco.contact.Birthday;
import com.jaco.contact.BirthdayDatabase;
import com.jaco.contact.R;
import com.jaco.contact.fileChooser.FileChooserActivity;

import java.util.List;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    public static final int FILE_CHOOSER_RESULT = 123;
    public static final int DRAW_OVERLAY = 4231;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference birthday_notifications = findPreference(getResources().getString(R.string.preference_birthday_notification_dialog));
        birthday_notifications.setOnPreferenceChangeListener(this);

        final Activity activity = getActivity();
        final Preference pref = findPreference(getResources().getString(R.string.preference_selected_db));
        pref.setPersistent(true);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onPreferenceClick(Preference preference) {

                //get shared preferences
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                //get source
                String path = settings.getString(getResources().getString(R.string.preference_selected_db), "");

                Intent intent = new Intent(activity, FileChooserActivity.class);
                intent.putExtra(FileChooserActivity.FILE_PATH, path);
                startActivityForResult(intent, FILE_CHOOSER_RESULT);
                return true;
            }
        });

        Preference pref_path = findPreference(getResources().getString(R.string.preference_selected_db_path));
        pref_path.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mSharedPreferences.setDatabasePath(activity, newValue.toString());
                return false;
            }
        });

        Preference flash_alert_pref = findPreference(getResources().getString(R.string.preference_flash_speed));
        flash_alert_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                buildFlashAlertDialog();
                return true;
            }
        });

        Preference alert_pref = findPreference(getResources().getString(R.string.preference_show_alert));
        alert_pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        !Settings.canDrawOverlays(SettingsFragment.this.getContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + SettingsFragment.this.getContext().getPackageName()));
                    startActivityForResult(intent, DRAW_OVERLAY);
                }
                return true;
            }
        });

    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (getResources().getString(R.string.preference_birthday_notification_dialog).equals(key)){
            int days_before = Integer.parseInt((String)newValue);
            new mAsyncTask(getActivity()).execute(days_before);
            return true;
        }
        return false;
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    public void buildFlashAlertDialog(){

        AlertDialog.Builder flash_alerts = new AlertDialog.Builder(getActivity());
        final View rootView_flash_alert = getActivity().getLayoutInflater().inflate(R.layout.flash_speed_setting, null);
        flash_alerts.setView(rootView_flash_alert);
        flash_alerts.setTitle(R.string.flash_time);

        final SeekBar on_time = (SeekBar) rootView_flash_alert.findViewById(R.id.on_time);
        final SeekBar off_time = (SeekBar) rootView_flash_alert.findViewById(R.id.off_time);

        on_time.setProgress(mSharedPreferences.getFlashOnTime(getActivity()));
        off_time.setProgress(mSharedPreferences.getFlashOffTime(getActivity()));

        flash_alerts.setNegativeButton(R.string.cancel, null);
        flash_alerts.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mSharedPreferences.setFlashOnTime(getActivity(), on_time.getProgress());
                mSharedPreferences.setFlashOffTime(getActivity(), off_time.getProgress());

            }
        });

        flash_alerts.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DRAW_OVERLAY){
            if (!Settings.canDrawOverlays(getContext())) {
                Toast.makeText(getContext(), R.string.no_permit_alert, Toast.LENGTH_LONG).show();
                mSharedPreferences.setShowAlert(getContext(), 4);
            }
        }

        if (resultCode == Activity.RESULT_OK){

            switch (requestCode){

                case FILE_CHOOSER_RESULT: {
                    //edit shared preferences
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        mSharedPreferences.setDatabasePath(getContext(), data.getStringExtra(FileChooserActivity.FILE_PATH));
                    else
                        mSharedPreferences.setDatabasePath(getActivity(), data.getStringExtra(FileChooserActivity.FILE_PATH));
                    break;
                }
            }

        }

    }

    public class mAsyncTask extends AsyncTask<Integer, Void, Void>{

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
