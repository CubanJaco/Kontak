package com.jaco.contact;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaco.headerrecyclerview.RecyclerViewHeaderAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 10/3/16.
 */
public class MenuAdapter extends RecyclerViewHeaderAdapter {

    private Context context;
    private List<Item> items;

    public MenuAdapter(Context context, int[] items_id, int[] summary_id, int[] icon_id){
        super(context, items_id.length);

        items = new ArrayList<>();

        for (int i = 0; i < items_id.length; i++) {
            items.add(new Item(items_id[i], icon_id[i],
                    context.getText(items_id[i]).toString(),
                    context.getText(summary_id[i]).toString()));
        }

        this.context = context;

    }

    public MenuAdapter(Context context, List items) {
        super(context, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public mHolder createHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.menu_adapter, viewGroup, false);
        return new mHolder(view);
    }

    @Override
    public void prepareItem(RecyclerViewHeaderAdapter.mHolder rHolder, int i) {

        mHolder holder = (mHolder) rHolder;
        holder.item.setText(items.get(i).getTitle());
        holder.summary.setText(items.get(i).getSummary());

        Drawable icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            icon = context.getDrawable(items.get(i).getIconId());
        else
            icon = context.getResources().getDrawable(items.get(i).getIconId());

        holder.icon.setImageDrawable(icon);

    }

    private void resetBackground(View view){

        // Create an array of the attributes we want to resolve
        // using values from a theme
        int[] attrs = new int[] { R.attr.buttonBackground/* index 0 */};

        // Obtain the styled attributes. 'themedContext' is a context with a
        // theme, typically the current Activity (i.e. 'this')
        TypedArray ta = view.getContext().obtainStyledAttributes(attrs);

        // To get the value of the 'buttonBackground' attribute that was
        // set in the theme used in 'themedContext'. The parameter is the index
        // of the attribute in the 'attrs' array. The returned Drawable
        // is what you are after
        Drawable background = ta.getDrawable(0 /* index */);

        // Finally, free the resources used by TypedArray
        ta.recycle();

//        Drawable background;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            background = context.getResources().getDrawable(R.drawable.layout_button, context.getTheme());
//        }
//        else
//            background = context.getResources().getDrawable(R.drawable.layout_button);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
            view.setBackgroundDrawable(background);
        else
            view.setBackground(background);
    }

    @Override
    public boolean setSelection(int selection) {

        if (isSingleChoice() && items.get(selection).isSelectable()){
            return super.setSelection(selection);
        }

        return true;

    }

    @Override
    public void setViewSelection(View view, int position ,boolean selection) {

        if (!selection){
            resetSelection(view, position);
            return;
        }

        int color;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            color = context.getColor(R.color.colorAccent);
        else
            color = context.getResources().getColor(R.color.colorAccent);

        TextView item = (TextView) view.findViewById(R.id.item_text);
        TextView summary = (TextView) view.findViewById(R.id.item_summary);
        ImageView icon = (ImageView) view.findViewById(R.id.menu_icon);

        if (items.get(position).isSelectable()){
            item.setTextColor(color);
            summary.setTextColor(color);
            icon.setColorFilter(color);
        }

        resetBackground(view);

    }

    public boolean clearSelection(){
        return setSelection(new boolean[items.size()]);
    }

    @Override
    public void resetSelection(View view, int position) {

        int color;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            color = context.getColor(R.color.white);
        else
            color = context.getResources().getColor(R.color.white);

        TextView item = (TextView) view.findViewById(R.id.item_text);
        item.setTextColor(color);

        TextView summary = (TextView) view.findViewById(R.id.item_summary);
        summary.setTextColor(color);

        ImageView icon = (ImageView) view.findViewById(R.id.menu_icon);
        icon.setColorFilter(color);

        resetBackground(view);

    }

    protected Item getItem(int i){
        return items.get(i);
    }

    protected int findItemById(int id){

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == id)
                return i;
        }

        return -1;
    }

    protected boolean setItemSelectable(int id, boolean selectable){

        int it = findItemById(id);

        if (it == -1)
            return false;

        items.get(it).setSelectable(selectable);
        return true;

    }

    @Override
    public int getItemsCount() {
        return items.size();
    }

    public static class Item{

        protected int id;
        protected int icon_id;
        protected String title;
        protected String summary;
        protected boolean selectable;

        public Item(int id, int icon_id, String title, String summary) {
            this.id = id;
            this.icon_id = icon_id;
            this.title = title;
            this.summary = summary;
            selectable = true;
        }

        public Item(int id, int icon_id, String title, String summary, boolean selectable) {
            this.id = id;
            this.icon_id = icon_id;
            this.title = title;
            this.summary = summary;
            this.selectable = selectable;
        }

        public int getId() {
            return id;
        }

        public int getIconId() {
            return icon_id;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public boolean isSelectable() {
            return selectable;
        }

        public void setSelectable(boolean selectable) {
            this.selectable = selectable;
        }
    }

    public class mHolder extends RecyclerViewHeaderAdapter.mHolder{

        protected View mItemView;
        protected TextView item;
        protected TextView summary;
        protected ImageView icon;

        public mHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            item = (TextView) mItemView.findViewById(R.id.item_text);
            summary = (TextView) mItemView.findViewById(R.id.item_summary);
            icon = (ImageView) mItemView.findViewById(R.id.menu_icon);
        }

    }
}
