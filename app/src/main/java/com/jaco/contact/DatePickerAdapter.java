package com.jaco.contact;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by osvel on 7/30/16.
 */
public class DatePickerAdapter extends RecyclerView.Adapter<DatePickerAdapter.mHolder> {

    private int selectedItem;
    private int colorTransparent;
    private String[] adapter;
    private RecyclerView recyclerView;
    private TextView change;
    private int lenght;
    private DatePickerAdapter listener;
    int[] month_days;

    public DatePickerAdapter(Context context, RecyclerView recyclerView, TextView change, String[] adapter, int lenght) {
        this.recyclerView = recyclerView;
        this.change = change;
        this.adapter = adapter;
        this.lenght = lenght;
        this.month_days = context.getResources().getIntArray(R.array.months_days);
        this.listener = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            colorTransparent = context.getResources().getColor(R.color.transparent, context.getTheme());
        else
            colorTransparent = context.getResources().getColor(R.color.transparent);
    }

    public void setListener(DatePickerAdapter listener) {
        this.listener = listener;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    @Override
    public mHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.date_picker_adapter, parent, false);
        return new mHolder(view);
    }

    public void setLenght(int lenght){
        if (lenght <= adapter.length){
            this.lenght = lenght;
        }

        if (lenght <= adapter.length && (lenght - 1) < selectedItem){
            setSelectedItem(lenght-1);
        }
    }

    public void setSelectedItem(int selectedItem) {
        if (selectedItem >= 0 && selectedItem < lenght) {
            this.selectedItem = selectedItem;
            change.setText(adapter[selectedItem].toUpperCase());
        }

        if (selectedItem >= 0 && selectedItem <= lenght && listener != null) {
            listener.setLenght(month_days[selectedItem]);
        }
    }

    @Override
    public void onBindViewHolder(final mHolder holder, final int position) {

        if (position == selectedItem)
            holder.layout_background.setBackgroundResource(R.drawable.selected_item_list);
        else
            holder.layout_background.setBackgroundColor(colorTransparent);

        holder.picker_text.setText(adapter[position].toUpperCase());

        holder.layout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedItem(position);
                recyclerView.swapAdapter(DatePickerAdapter.this, false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lenght;
    }

    public class mHolder extends RecyclerView.ViewHolder {

        protected View mView;
        protected LinearLayout layout_background;
        protected LinearLayout layout_button;
        protected TextView picker_text;

        public mHolder(View itemView) {
            super(itemView);
            mView = itemView;
            layout_button = (LinearLayout) itemView.findViewById(R.id.layout_button);
            layout_background = (LinearLayout) itemView.findViewById(R.id.layout_background);
            picker_text = (TextView) itemView.findViewById(R.id.picker_text);
        }
    }
}
