package com.jaco.contact;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;

/**
 * Created by osvel on 7/13/16.
 */
public class ContactsItemList extends RecyclerView.Adapter<ContactsItemList.mViewHolder> {

    private PhoneEntry[] phoneEntries;

    public ContactsItemList(PhoneEntry[] phoneEntries) {
        this.phoneEntries = phoneEntries;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item_list, parent, false);
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, final int position) {

        final Context context = holder.itemView.getContext();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onclick de la lista
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra(ProfileActivity.PHONE_ENTRY, phoneEntries[position]);
                context.startActivity(intent);
            }
        });

        holder.name.setText(phoneEntries[position].getName());
        holder.number.setText(phoneEntries[position].getNumber());
    }

    @Override
    public int getItemCount() {
        return phoneEntries.length;
    }

    public class mViewHolder extends RecyclerView.ViewHolder implements Serializable {

        protected View itemView;
        protected TextView name;
        protected TextView number;

        public mViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            name = (TextView) itemView.findViewById(R.id.contact_name);
            number = (TextView) itemView.findViewById(R.id.contact_number);
        }
    }
}
