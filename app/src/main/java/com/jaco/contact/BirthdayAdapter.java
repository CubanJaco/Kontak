package com.jaco.contact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by osvel on 7/27/16.
 */
public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.mHolder> {

    private Object[] birthdays;
    private boolean show_ignore;

    public BirthdayAdapter(Object[] bd) {

        Arrays.sort(bd, new Comparator<Object>() {
            @Override
            public int compare(Object ob_0, Object ob_1) {
                Birthday bd_0 = (Birthday) ob_0;
                Birthday bd_1 = (Birthday) ob_1;
                return bd_0.getBirthday().compareTo(bd_1.getBirthday());
            }
        });

        this.birthdays = bd;

    }

    public BirthdayAdapter(List<Birthday> birthdays, boolean show_ignore) {

        for (int i = 0; i < birthdays.size(); i++) {
            if (birthdays.get(i).isIgnored() && !show_ignore){
                birthdays.remove(i);
            }
        }

//        Object[] bd = birthdays.toArray();
//        Arrays.sort(bd, new Comparator<Object>() {
//            @Override
//            public int compare(Object ob_0, Object ob_1) {
//                Birthday bd_0 = (Birthday) ob_0;
//                Birthday bd_1 = (Birthday) ob_1;
//                return bd_0.getBirthday().compareTo(bd_1.getBirthday());
//            }
//        });

        this.birthdays = birthdays.toArray();
        this.show_ignore = show_ignore;

    }

    public Birthday getItem(int position){
        return (Birthday)birthdays[position];
    }

    @Override
    public mHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_log_adapter, parent, false);
        return new mHolder(view);
    }

    @Override
    public void onBindViewHolder(mHolder holder, int position) {

        final Birthday birthday = (Birthday) birthdays[position];
        final Context context = holder.mView.getContext();

        //preparar la dimension de la imagen
        int dim = (int) context.getResources().getDimension(R.dimen.icon_size);

        String image_uri = birthday.getImageUri();
        if (image_uri != null && image_uri.length() > 0){
            Picasso.with(context)
                    .load(image_uri)
                    .resize(dim, dim)
                    .transform(MainActivity.transformation)
                    .into(holder.contact_image,
                            getPicassoCallback(context, holder.contact_image, dim));
        }
        else {
            Picasso.with(context)
                    .load(R.drawable.user)
                    .resize(dim, dim)
                    .transform(MainActivity.transformation)
                    .into(holder.contact_image);
        }

        //obtener color rojo
        int redColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            redColor = context.getResources().getColor(R.color.colorRed, context.getTheme());
        else
            redColor = context.getResources().getColor(R.color.colorRed);

        //establecer datos al adapter
        holder.contact_name.setText(birthday.getContactName());
        if (!birthday.isCustom())
            holder.contact_number.setText(birthday.getOwnerName());
        else
            holder.contact_number.setText("");

        if (birthday.isIgnored()){
            holder.call_identifier.setImageResource(R.drawable.icon_cancel);
            holder.call_identifier.setColorFilter(redColor);
            holder.call_identifier.setVisibility(View.VISIBLE);
        }
        else
            holder.call_identifier.setVisibility(View.GONE);

        holder.call_duration.setText(birthday.getStringBirthday());
        holder.call_time.setText(birthday.getLeftTime(context));

        //ocultar el layout del dia de llamada
        holder.call_day_layout.setVisibility(View.GONE);

        PhoneEntry[] phoneEntry = null;
        if (PhoneNumber.isValidNumber(birthday.getContactNumber()))
            phoneEntry = new EtecsaDB(context).searchByNumber(birthday.getContactNumber());

        //establecer el onclick
        final PhoneEntry[] finalPhoneEntry = phoneEntry;
        holder.call_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (finalPhoneEntry != null && finalPhoneEntry.length != 0){
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.PHONE_ENTRY, finalPhoneEntry[0]);
                    context.startActivity(intent);
                }
                else
                    buildMenu(context, birthday);
            }
        });

        //establecer el onlongclick
        holder.call_log.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buildMenu(context, birthday);
                return true;
            }
        });

    }

    public void buildMenu(final Context context, final Birthday birthday){

        final RecyclerView recyclerView = (RecyclerView) ((AppCompatActivity)context).findViewById(R.id.recycler_view_list);

        AlertDialog.Builder menu_dialog = new AlertDialog.Builder(context);
        //select options array
        String[] options;
        if (birthday.isCustom() && birthday.getOwnerName().length() == 0){
            options = context.getResources().getStringArray(R.array.custom_birthday_options);
        }
        else {
            options = new String[2];
            if (birthday.isIgnored())
                options[0] = context.getResources().getString(R.string.notify);
            else
                options[0] = context.getResources().getString(R.string.ignore);
            if (birthday.isCustom())
                options[1] = context.getResources().getString(R.string.reset);
            else
                options[1] = context.getResources().getString(R.string.customize);
        }

        menu_dialog.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                BirthdayDatabase database = new BirthdayDatabase(context);
                switch (i) {
                    case 0: {
                        if (birthday.isCustom() && birthday.getOwnerName().length() == 0) {
                            database.delete(birthday);
                        } else if (birthday.isIgnored()) {
                            birthday.setIgnored(false);
                            database.setIgnored(birthday);
                        } else {
                            birthday.setIgnored(true);
                            database.setIgnored(birthday);
                        }

                        new mAsyncTask(context, recyclerView).execute(database);

                        break;
                    }
                    case 1: {
                        if (!birthday.isCustom())
                            buildCustomDialog(context, recyclerView, birthday);
                        else {

                            birthday.setCustom(null);
                            database.setCustomBirthday(birthday);

                            new mAsyncTask(context, recyclerView).execute(database);

                        }
                        break;
                    }
                }
            }
        });
        menu_dialog.show();

    }

    public void buildCustomDialog(final Context context, final RecyclerView birthday_recycler, final Birthday birthday){

        final View custom_birthday_rootView = ((AppCompatActivity)context).getLayoutInflater().inflate(R.layout.custom_birthday_dialog, null);
        final RecyclerView recyclerView = (RecyclerView) custom_birthday_rootView.findViewById(R.id.date_picker_list);

        final EditText name_ediEditText = (EditText) custom_birthday_rootView.findViewById(R.id.contact_name);
        name_ediEditText.setVisibility(View.GONE);

        //crear los arreglos de String para los dos selectores
        final String[] days = context.getResources().getStringArray(R.array.day_of_month);
        final String[] month = context.getResources().getStringArray(R.array.months);
        int[] month_days = context.getResources().getIntArray(R.array.months_days);

        //obtener las vistas para mostrar la fecha
        TextView day_text = (TextView) custom_birthday_rootView.findViewById(R.id.day_picker);
        TextView month_text = (TextView) custom_birthday_rootView.findViewById(R.id.month_picker);

        final GregorianCalendar calendar = new GregorianCalendar();
        int current_month = calendar.get(Calendar.MONTH);
        int current_day = calendar.get(Calendar.DAY_OF_MONTH)-1;

        //crear los dos adapters
        final DatePickerAdapter days_adapter = new DatePickerAdapter(context, recyclerView, day_text, days, month_days[current_month]);
        final DatePickerAdapter month_adapter = new DatePickerAdapter(context, recyclerView, month_text, month, month.length);

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
        AlertDialog.Builder custom_birthday = new AlertDialog.Builder(context);
        custom_birthday.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int current_year = calendar.get(Calendar.YEAR);
                calendar.setTimeInMillis(Birthday.PLUS_TIME);
                calendar.set(Calendar.MONTH, month_adapter.getSelectedItem());
                calendar.set(Calendar.DAY_OF_MONTH, days_adapter.getSelectedItem() + 1);
                calendar.set(Calendar.YEAR, current_year);

                birthday.setCustom(calendar);
                birthday.setSchedule(context);
                BirthdayDatabase database = new BirthdayDatabase(context);
                database.setCustomBirthday(birthday);

                birthday_recycler.swapAdapter(new BirthdayAdapter(birthdays), false);
            }
        });
        custom_birthday.setNegativeButton(R.string.cancel, null);
        custom_birthday.setView(custom_birthday_rootView);
        custom_birthday.setTitle(R.string.customize);
        custom_birthday.show();

    }

    public Callback getPicassoCallback(final Context context, final ImageView imageView, final int dim){

        return new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(context)
                        .load(R.drawable.user)
                        .resize(dim, dim)
                        .transform(MainActivity.transformation)
                        .into(imageView);
            }
        };

    }

    @Override
    public int getItemCount() {
        return birthdays.length;
    }

    public class mAsyncTask extends AsyncTask<BirthdayDatabase, Void, List<Birthday>>{

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;
        protected RecyclerView recyclerView;

        public mAsyncTask(Context context, RecyclerView recyclerView) {
            this.weakReference = new WeakReference<>((Activity)context);
            this.recyclerView = recyclerView;
        }

        @Override
        protected void onPreExecute() {

            final Activity activity = weakReference.get();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = ProgressDialog.show(activity, "",
                            activity.getString(R.string.searching), true);
                }
            });

        }

        @Override
        protected List<Birthday> doInBackground(BirthdayDatabase... databases) {

            return databases[0].getAllBirthdays(show_ignore);

        }

        @Override
        protected void onPostExecute(List<Birthday> birthdays_list) {

            recyclerView.swapAdapter(new BirthdayAdapter(birthdays_list, show_ignore), false);
            progressDialog.dismiss();

        }
    }

    public class mHolder extends RecyclerView.ViewHolder {

        protected View mView;
        protected LinearLayout call_day_layout;
        protected TextView call_day;
        protected LinearLayout call_log;
        protected ImageView contact_image;
        protected ImageView call_identifier;
        protected TextView contact_name;
        protected TextView contact_number;
        protected TextView call_time;
        protected TextView call_duration;

        public mHolder(View itemView) {
            super(itemView);
            mView = itemView;
            call_day_layout = (LinearLayout) mView.findViewById(R.id.call_day_layout);
            call_day = (TextView) mView.findViewById(R.id.call_day);
            call_log = (LinearLayout) mView.findViewById(R.id.call_log);
            contact_image = (ImageView) mView.findViewById(R.id.contact_image);
            call_identifier = (ImageView) mView.findViewById(R.id.call_identifier);
            contact_name = (TextView) mView.findViewById(R.id.contact_name);
            contact_number = (TextView) mView.findViewById(R.id.contact_number);
            call_time = (TextView) mView.findViewById(R.id.call_hour);
            call_duration = (TextView) mView.findViewById(R.id.call_duration);
        }
    }
}
