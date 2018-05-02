package com.jaco.contact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by osvel on 7/28/16.
 */
public class BirthdayFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "BirthdayFragment";

    private boolean show_ignore;

    public BirthdayFragment() {}

    public static Fragment newInstance() {
        return new BirthdayFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_birthday, container, false);

        new CreateAdapter(getActivity()).execute();

        show_ignore = false;

        ImageView show_ignore_icon = (ImageView) rootview.findViewById(R.id.show_ignore_icon);
        show_ignore_icon.setImageResource(R.drawable.icon_visibility_show);

        LinearLayout button = (LinearLayout) rootview.findViewById(R.id.button_custom_birthday);
        button.setOnClickListener(this);
        button.setOnLongClickListener(this);
        button = (LinearLayout) rootview.findViewById(R.id.button_show_ignore);
        button.setOnClickListener(this);
        button.setOnLongClickListener(this);
        button = (LinearLayout) rootview.findViewById(R.id.button_sync);
        button.setOnClickListener(this);
        button.setOnLongClickListener(this);

        return rootview;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        ProfileActivity.onClickAnimation(view);

        switch (id){
            case R.id.button_show_ignore: {
                show_ignore = !show_ignore;

                new CreateAdapter(getActivity()).execute();

                ImageView show_ignore_icon = (ImageView) getActivity().findViewById(R.id.show_ignore_icon);
                if (show_ignore)
                    show_ignore_icon.setImageResource(R.drawable.icon_visibility_hide);
                else
                    show_ignore_icon.setImageResource(R.drawable.icon_visibility_show);

                break;
            }
            case R.id.button_sync: {

                new CreateAdapter(getActivity(), true).execute();

                break;
            }
            case R.id.button_custom_birthday: {

                buildCustomDialog();

                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.button_show_ignore: {
                Toast.makeText(getContext(), R.string.show_ignore, Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.button_sync: {
                Toast.makeText(getContext(), R.string.sync, Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.button_custom_birthday: {
                Toast.makeText(getContext(), R.string.add_custom_birthday, Toast.LENGTH_SHORT).show();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public void buildCustomDialog(){

        final View custom_birthday_rootView = getActivity().getLayoutInflater().inflate(R.layout.custom_birthday_dialog, null);
        final RecyclerView recyclerView = (RecyclerView) custom_birthday_rootView.findViewById(R.id.date_picker_list);

        //crear los arreglos de String para los dos selectores
        final String[] days = getResources().getStringArray(R.array.day_of_month);
        final String[] month = getResources().getStringArray(R.array.months);
        int[] month_days = getResources().getIntArray(R.array.months_days);

        //obtener las vistas para mostrar la fecha
        TextView day_text = (TextView) custom_birthday_rootView.findViewById(R.id.day_picker);
        TextView month_text = (TextView) custom_birthday_rootView.findViewById(R.id.month_picker);

        final GregorianCalendar calendar = new GregorianCalendar();
        int current_month = calendar.get(Calendar.MONTH);
        int current_day = calendar.get(Calendar.DAY_OF_MONTH)-1;

        //crear los dos adapters
        final DatePickerAdapter days_adapter = new DatePickerAdapter(getActivity(), recyclerView, day_text, days, month_days[current_month]);
        final DatePickerAdapter month_adapter = new DatePickerAdapter(getActivity(), recyclerView, month_text, month, month.length);

        month_adapter.setListener(days_adapter);

        //poner en el dialogo la fecha actual
        month_adapter.setSelectedItem(current_month);
        days_adapter.setSelectedItem(current_day);


        //establecer inicialmente el adapter de selector de numeros
        recyclerView.setAdapter(days_adapter);
        recyclerView.scrollToPosition(days_adapter.getSelectedItem());

        day_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setAdapter(days_adapter);
                recyclerView.scrollToPosition(days_adapter.getSelectedItem());
            }
        });

        month_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setAdapter(month_adapter);
                recyclerView.scrollToPosition(month_adapter.getSelectedItem());
            }
        });

        //crear el dialogo con la vista del picker
        AlertDialog.Builder custom_birthday = new AlertDialog.Builder(getActivity());
        custom_birthday.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText name_ediEditText = (EditText) custom_birthday_rootView.findViewById(R.id.contact_name);
                int current_year = calendar.get(Calendar.YEAR);
                calendar.setTimeInMillis(Birthday.PLUS_TIME);
                calendar.set(Calendar.MONTH, month_adapter.getSelectedItem());
                calendar.set(Calendar.DAY_OF_MONTH, days_adapter.getSelectedItem()+1);
                calendar.set(Calendar.YEAR, current_year);
                String name = name_ediEditText.getText().toString();

                if (name.length() != 0) {
                    Birthday bd = new Birthday(name, calendar);
                    BirthdayDatabase database = new BirthdayDatabase(BirthdayFragment.this.getContext());
                    database.addBirthday(bd);
                    bd.setSchedule(BirthdayFragment.this.getContext());
                    Toast.makeText(BirthdayFragment.this.getContext(), R.string.birthday_added, Toast.LENGTH_SHORT).show();
                    new CreateAdapter(BirthdayFragment.this.getActivity()).execute();
                }
                else
                    Toast.makeText(BirthdayFragment.this.getContext(), R.string.empty_name, Toast.LENGTH_SHORT).show();

            }
        });
        custom_birthday.setNegativeButton(R.string.cancel, null);
        custom_birthday.setView(custom_birthday_rootView);
        custom_birthday.setTitle(R.string.custom);
        custom_birthday.show();

    }

    public void insertBirthdays(BirthdayDatabase database){

        Contacts contacts = Contacts.getInstance(getContext());
        List<ContactDetail> contactDetails = contacts.getContacts();
        for (ContactDetail detail : contactDetails) {

            PhoneNumber phoneNumber = new PhoneNumber(detail.getNumber());
            if (phoneNumber.isMovil()){
                Birthday birthday = new Birthday(getContext(), detail);
                boolean inserted = false;
                if (birthday.isValid()){
                    inserted = database.addBirthday(birthday);
                }

                if (inserted)
                    Log.i(TAG, "insertBirthdays: birthday inserted for "+birthday.getContactNumber());
                else
                    Log.i(TAG, "insertBirthdays: birthday not inserted for "+birthday.getContactNumber());
            }

        }

    }

    public void syncBirthdays(BirthdayDatabase database){

        Contacts contacts = Contacts.getInstance(getContext());
        List<ContactDetail> contactDetails = contacts.getContacts();
        List<String> numbers = new ArrayList<>();
        for (ContactDetail detail : contactDetails) {

            numbers.add(detail.getNumber());

            PhoneNumber phoneNumber = new PhoneNumber(detail.getNumber());
            if (phoneNumber.isMovil()){
                Birthday birthday = new Birthday(getContext(), detail);
                if (birthday.isValid()){
                    database.syncBirthday(birthday);
                }
            }

        }

        List<Birthday> birthdays = database.getAllBirthdays(true);
        for (Birthday bd : birthdays) {
            if (!bd.getContactNumber().equals(Birthday.CUSTOM_NUMBER) && !numbers.contains(bd.getContactNumber())){
                database.delete(bd);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        //ocultar campo de busqueda
        final LinearLayout search_layout = (LinearLayout) getActivity().findViewById(R.id.search_layout);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_to_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                search_layout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (search_layout.getVisibility() != View.GONE)
            search_layout.startAnimation(animation);

    }

    private class InsertBirthdays extends AsyncTask<BirthdayDatabase, Void, List<Birthday>>{

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;
        protected BirthdayDatabase database;

        public InsertBirthdays(Activity activity, ProgressDialog progressDialog) {
            this.weakReference = new WeakReference<>(activity);
            this.progressDialog = progressDialog;
        }

        @Override
        protected List<Birthday> doInBackground(BirthdayDatabase... databases) {

            this.database = databases[0];
            insertBirthdays(database);

            List<Birthday> birthdays = database.getAllBirthdays(show_ignore);
            for (Birthday bd : birthdays){
                bd.setSchedule(weakReference.get());
            }

            return birthdays;
        }

        @Override
        protected void onPostExecute(List<Birthday> birthdays) {

            RecyclerView recyclerView = (RecyclerView) weakReference.get().findViewById(R.id.recycler_view_list);
            recyclerView.setAdapter(new BirthdayAdapter(birthdays, show_ignore));
            Animation animation = AnimationUtils.loadAnimation(weakReference.get(), R.anim.enter_from_bottom);
            animation.setStartOffset(200);
            recyclerView.startAnimation(animation);

            progressDialog.dismiss();
        }
    }

    private class CreateAdapter extends AsyncTask<Void, Void, List<Birthday>>{

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;
        protected boolean sync;

        public CreateAdapter(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
            this.sync = false;
        }

        public CreateAdapter(Activity activity, boolean sync) {
            this.weakReference = new WeakReference<>(activity);
            this.sync = sync;
        }

        @Override
        protected void onPreExecute() {

            RecyclerView recyclerView;
            try {
                recyclerView = (RecyclerView) weakReference.get().findViewById(R.id.recycler_view_list);
            }
            catch (NullPointerException e){
                e.printStackTrace();
                recyclerView = null;
            }

            final Activity activity = weakReference.get();
            final RecyclerView finalRecyclerView = recyclerView;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = ProgressDialog.show(activity, "",
                            activity.getString(R.string.getting), true);

                    if (finalRecyclerView != null) {
                        finalRecyclerView.startAnimation(
                                AnimationUtils.loadAnimation(weakReference.get(), R.anim.exit_to_bottom)
                        );
                        finalRecyclerView.setVisibility(View.GONE);
                    }
                }
            });

        }

        @Override
        protected List<Birthday> doInBackground(Void... voids) {

            BirthdayDatabase database = new BirthdayDatabase(weakReference.get());

            if (sync) {
                syncBirthdays(database);
                List<Birthday> birthdays = database.getAllBirthdays(true);
                for (Birthday bd : birthdays)
                    bd.setSchedule(weakReference.get());
            }

            return database.getAllBirthdays(show_ignore);
        }

        @Override
        protected void onPostExecute(List<Birthday> birthdays) {

            EtecsaDB etecsaDB = new EtecsaDB(weakReference.get());
            if (birthdays.size() == 0 && etecsaDB.hasDatabase()){
                new InsertBirthdays(getActivity(), progressDialog).execute(new BirthdayDatabase(weakReference.get()));
            }
            else {
                RecyclerView recyclerView = (RecyclerView) weakReference.get().findViewById(R.id.recycler_view_list);
                recyclerView.swapAdapter(new BirthdayAdapter(birthdays, show_ignore), false);
                recyclerView.startAnimation(AnimationUtils.loadAnimation(weakReference.get(), R.anim.enter_from_bottom));
                recyclerView.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
            }
        }
    }

}
