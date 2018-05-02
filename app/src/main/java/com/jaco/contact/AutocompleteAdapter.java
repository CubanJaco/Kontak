package com.jaco.contact;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 7/24/16.
 */
public class AutocompleteAdapter extends ArrayAdapter<ContactDetail>{

    private List<ContactDetail> contacts;
    private List<ContactDetail> contactsAll;
    private Context mContext;

    public AutocompleteAdapter(Context context, List<ContactDetail> contactsAll) {
        super(context, R.layout.contact_adapter, contactsAll);
        this.mContext = context;
        this.contactsAll = new ArrayList<>(contactsAll);
        this.contacts = new ArrayList<>(contactsAll);
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public ContactDetail getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) this.mContext).getLayoutInflater().inflate(R.layout.contact_adapter, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.contact_name);
        TextView number = (TextView) convertView.findViewById(R.id.contact_number);
        final ImageView contact_image = (ImageView) convertView.findViewById(R.id.contact_image);

        final int dim = (int) mContext.getResources().getDimension(R.dimen.avatar_size);

        Uri displayPhotoUri = Contacts.getInstance(mContext).getUriDisplayPhoto(getItem(position).getNumber());

        Callback callback = new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(mContext)
                        .load(R.drawable.user)
                        .resize(dim, dim)
                        .transform(MainActivity.transformation)
                        .into(contact_image);
            }
        };

        Picasso.with(mContext)
                .load(displayPhotoUri)
                .resize(dim, dim)
                .transform(MainActivity.transformation)
                .into(contact_image, callback);

        ContactDetail contact_detail = getItem(position);
        name.setText(contact_detail.getName());
        number.setText(contact_detail.getNumber());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new mFilter();
    }

    private class mFilter extends Filter{

        protected mFilter() {}

        public String convertResultToString(Object resultValue) {
            return ((ContactDetail) resultValue).getNumber().replace(" ", "");
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            if (charSequence == null) {
                return new FilterResults();
            }

            List<ContactDetail> mContactsSuggested = new ArrayList<>();

            for (ContactDetail contact : contactsAll) {
                if (contact.getNumber().replace(" ", "").contains(charSequence.toString().replace(" ", "")) || contact.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                    mContactsSuggested.add(contact);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = mContactsSuggested;
            filterResults.count = mContactsSuggested.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            contacts.clear();
            if (filterResults != null && filterResults.count > 0) {
                for (Object object : (List) filterResults.values) {
                    if (object instanceof ContactDetail) {
                        contacts.add((ContactDetail) object);
                    }
                }
            } else if (charSequence == null) {
                contacts.addAll(contactsAll);
            }
            AutocompleteAdapter.this.notifyDataSetChanged();
        }
    }
}
