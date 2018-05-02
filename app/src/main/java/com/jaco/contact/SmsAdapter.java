package com.jaco.contact;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 7/27/16.
 */
public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.mHolder> {

    private List<SmsItem> items;

    public SmsAdapter(int[] items, int[] summary, int[] icons) {

        this.items = new ArrayList<>();

        for (int i = 0; i < items.length; i++)
            this.items.add(new SmsItem(items[i], summary[i], icons[i]));

    }

    public SmsAdapter(List<SmsItem> items) {
        this.items = items;
    }

    @Override
    public mHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_services_adapter, parent, false);
        return new mHolder(view);
    }

    @Override
    public void onBindViewHolder(mHolder holder, int position) {

        SmsItem item = items.get(position);

        holder.item.setOnClickListener(new ItemClick(item.getId()));

        holder.item_title.setText(item.getTitle());
        holder.item_summary.setText(item.getSummary());
        holder.icon.setImageResource(item.getIcon());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void itemClick(Context context, int id){

        switch (id){
            case R.string.weather: {
                String[] array = context.getResources().getStringArray(R.array.weather);
                String[] message = context.getResources().getStringArray(R.array.weather_msg);
                selectDialog(context, "8888", array, message);
                break;
            }
            case R.string.exchange: {
                String[] array = context.getResources().getStringArray(R.array.exchange);
                String[] message = context.getResources().getStringArray(R.array.exchange_msg);
                selectDialog(context, "8888", array, message);
                break;
            }
            case R.string.programs: {
                String[] array = context.getResources().getStringArray(R.array.programs);
                String[] message = context.getResources().getStringArray(R.array.programs_msg);
                selectDialog(context, "8888", array, message);
                break;
            }
        }

    }

    public void selectDialog(Context context, final String number, String[] array, final String[] message){

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.weather_status);

        dialog.setItems(array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.sendSMS(number, message[i]);
            }
        });

        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();

    }

    public static class SmsItem{

        protected int id;
        protected int icon;
        protected int title;
        protected int summary;

        public SmsItem(int title, int summary, int icon) {
            id = title;
            this.title = title;
            this.summary = summary;
            this.icon = icon;
        }

        public int getId() {
            return id;
        }

        public int getIcon() {
            return icon;
        }

        public int getTitle() {
            return title;
        }

        public int getSummary() {
            return summary;
        }
    }

    public class ItemClick implements View.OnClickListener{

        protected int id;

        public ItemClick(int id) {
            this.id = id;
        }

        @Override
        public void onClick(View view) {
            itemClick(view.getContext(), id);
        }

    }

    public class mHolder extends RecyclerView.ViewHolder {

        protected View mView;
        protected LinearLayout item;
        protected ImageView icon;
        protected TextView item_title;
        protected TextView item_summary;

        public mHolder(View itemView) {
            super(itemView);
            mView = itemView;

            item = (LinearLayout) mView.findViewById(R.id.item);
            icon = (ImageView) mView.findViewById(R.id.icon);
            item_title = (TextView) mView.findViewById(R.id.item_title);
            item_summary = (TextView) mView.findViewById(R.id.item_summary);

        }
    }
}
