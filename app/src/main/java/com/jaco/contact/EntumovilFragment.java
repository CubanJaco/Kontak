package com.jaco.contact;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class EntumovilFragment extends Fragment {

    private static final int[] title = {
            R.string.weather,
            R.string.exchange,
            R.string.programs,
            R.string.dhl,
            R.string.embassy,
            R.string.quotes,
            R.string.football,
            R.string.havanatur,
            R.string.tourism,
            R.string.chinese_zodiac,
            R.string.zodiac,
            R.string.baseball,
            R.string.recipes,
            R.string.marti };

    private static final int[] summary = {
            R.string.weather_summary,
            R.string.exchange_summary,
            R.string.programs_summary,
            R.string.dhl_summary,
            R.string.embassy_summary,
            R.string.quotes_summary,
            R.string.football_summary,
            R.string.havanatur_summary,
            R.string.tourism_summary,
            R.string.chinese_zodiac_summary,
            R.string.zodiac_summary,
            R.string.baseball_summary,
            R.string.recipes_summary,
            R.string.marti_summary };

    private static final int[] icons = {
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
            R.drawable.icon_settings,
    };

    public static Fragment newInstance() {
        EntumovilFragment fragment = new EntumovilFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootview.findViewById(R.id.recycler_view_list);
        SmsAdapter adapter = new SmsAdapter(title, summary, icons);
        recyclerView.setAdapter(adapter);

        return rootview;
    }

}
