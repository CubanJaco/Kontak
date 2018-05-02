package com.jaco.contact;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.jaco.contact.preferences.mSharedPreferences;

/**
 * Created by osvel on 8/5/16.
 */
public class WidgetCallCredit extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_call_credit);
            Intent active = new Intent(context, WidgetContainerActivity.class);
            active.putExtra(WidgetContainerActivity.FRAGMENT_INDEX, WidgetContainerActivity.FREE_CALL);
            active.setAction(Utils.ACTION_CALL);
            active.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            views.setOnClickPendingIntent(R.id.button_free_call, PendingIntent.getActivity(context, 0, active, 0));

            String credit = mSharedPreferences.getCreditConsult(context);
            Uri uri = Uri.parse("tel:" + Uri.encode(credit));
            active = new Intent(Intent.ACTION_CALL);
            active.setData(uri);
            views.setOnClickPendingIntent(R.id.button_credit, PendingIntent.getActivity(context, 0, active, 0));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
