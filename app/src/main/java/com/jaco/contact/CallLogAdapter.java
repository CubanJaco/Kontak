package com.jaco.contact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 7/27/16.
 */
public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.mHolder> {

    private List<CallDetails> calls;

    public CallLogAdapter(List<CallDetails> calls) {
        this.calls = calls;
    }

    public void addAll(List<CallDetails> calls){
        if (this.calls == null)
            this.calls = new ArrayList<>();
        this.calls.addAll(calls);
    }

    @Override
    public mHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.call_log_adapter, parent, false);
        return new mHolder(view);
    }

    @Override
    public void onBindViewHolder(final mHolder holder, int position) {

        CallDetails call = calls.get(position);
        final Context context = holder.mView.getContext();

        //preparar la dimension de la imagen
        int dim = (int) context.getResources().getDimension(R.dimen.icon_size);

        //si es un contacto intentar cargar la imagen o de lo contrario cargar R.drawable.user
        if (call.getImageUri() != null){
            Uri displayPhotoUri = call.getImageUri();
            Picasso.with(context)
                    .load(displayPhotoUri)
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

        //falta cargar la imagen del tipo de llamada
        int resId = -1;
        int color = -1;
        switch (call.getCall().getType()){
            case CallLog.Calls.MISSED_TYPE: {
                resId = R.drawable.call_missed;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    color = context.getResources().getColor(R.color.colorRed, context.getTheme());
                else
                    color = context.getResources().getColor(R.color.colorRed);
                break;
            }
            case CallLog.Calls.INCOMING_TYPE: {
                resId = R.drawable.call_received;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    color = context.getResources().getColor(R.color.colorBlue, context.getTheme());
                else
                    color = context.getResources().getColor(R.color.colorBlue);
                break;
            }
            case CallLog.Calls.OUTGOING_TYPE: {
                resId = R.drawable.call_made;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    color = context.getResources().getColor(R.color.colorGreen, context.getTheme());
                else
                    color = context.getResources().getColor(R.color.colorGreen);
                break;
            }
        }

        if (resId != -1)
            holder.call_identifier.setImageResource(resId);
        if (color != -1)
            holder.call_identifier.setColorFilter(color);

        //establecer datos al adapter
        holder.contact_name.setText(call.getName());
        holder.contact_number.setText(call.getCall().getNumber());
        holder.call_duration.setText(call.getCall().getDuration());
        holder.call_time.setText(call.getCall().getStringTime());

        if (position == 0 || !call.getCall().getDay().equals(calls.get(position-1).getCall().getDay())){
            holder.call_day_layout.setVisibility(View.VISIBLE);
            holder.call_day.setText(call.getCall().getDay());
        }
        else {
            holder.call_day_layout.setVisibility(View.GONE);
        }

        final PhoneNumber phoneNumber = new PhoneNumber(call.getCall().getNumber());
        holder.call_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findContact(context, phoneNumber.getNumber());
            }
        });

        if (position == getItemCount()-1 && getItemCount() % 25 == 0){
            holder.load_more.setVisibility(View.VISIBLE);
            final int page = (getItemCount() / 25) + 1;
            holder.load_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyCallLog callLog = new MyCallLog(holder.mView.getContext());
                    new CallLogFragment.mAsyncTask(
                            (Activity) holder.mView.getContext()
                    ).execute(callLog.getCallLog(page));
                }
            });
        }
        else {
            holder.load_more.setVisibility(View.GONE);
        }

    }

    public void findContact(Context context, String number){
        //verificar la base de datos
//        EtecsaDB database = new EtecsaDB(context);
//        if (!database.hasDatabase()){
//            Toast.makeText(context, R.string.no_database, Toast.LENGTH_SHORT).show();
//        }

        if (PhoneNumber.isValidNumber(number))
            new Search(context).execute(number);
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
        return calls.size();
    }

    public class mHolder extends RecyclerView.ViewHolder {

        protected View mView;
        protected LinearLayout load_more;
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
            load_more = (LinearLayout) mView.findViewById(R.id.load_more);
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

    private class Search extends AsyncTask<String, Void, PhoneEntry[]> {

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;

        public Search(Context context) {
            this.weakReference = new WeakReference<>((Activity)context);
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
        protected PhoneEntry[] doInBackground(String... numbers) {

            String number = numbers[0];
            PhoneNumber phoneNumber = new PhoneNumber(number);

            //Abrir la conexion a la base de datos
            EtecsaDB etecsaDB = new EtecsaDB(weakReference.get());

            PhoneEntry phoneEntry[] = null;
            if (etecsaDB.hasDatabase() && PhoneNumber.isValidNumber(number)){
                phoneEntry = etecsaDB.searchByNumber(phoneNumber.getNumber());
            }

            //cerrar la conexion a la base de datos
            etecsaDB.close();

            if (phoneEntry == null || phoneEntry.length == 0){

                phoneEntry = new PhoneEntry[1];
                phoneEntry[0] = new PhoneEntry(number);
                phoneEntry[0].setType(PhoneType.UNKNOWN);

            }

            return phoneEntry;
        }

        @Override
        protected void onPostExecute(PhoneEntry[] phoneEntry) {

            if (phoneEntry.length > 1){
                //encontro algo entonces abrir el nuevo fragment
                AppCompatActivity activity = (AppCompatActivity) weakReference.get();
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .addToBackStack(null)
                        .replace(R.id.container, PhonesListFragment.newInstance(phoneEntry))
                        .commit();
            }
            else {
                AppCompatActivity activity = (AppCompatActivity) weakReference.get();
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.PHONE_ENTRY, phoneEntry[0]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                }
                else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                }
                activity.startActivity(intent);
            }
//            else {
//                //no encontro nada mostrar mensaje
//                AppCompatActivity activity = (AppCompatActivity) weakReference.get();
//                Intent intent = new Intent(activity, ProfileActivity.class);
//                intent.putExtra(ProfileActivity.PHONE_ENTRY, phoneEntry[0]);
//                activity.startActivity(intent);
//
//                weakReference.get().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(weakReference.get().getApplicationContext(), R.string.not_matches, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }

            progressDialog.dismiss();
        }
    }

}
