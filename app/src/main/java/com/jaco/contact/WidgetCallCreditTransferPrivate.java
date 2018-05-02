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
public class WidgetCallCreditTransferPrivate extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_call_credit_transfer_private);

            Intent free_call = new Intent(context, WidgetContainerActivity.class);
            free_call.putExtra(WidgetContainerActivity.FRAGMENT_INDEX, WidgetContainerActivity.FREE_CALL);
            free_call.setAction(Utils.ACTION_FREE_CALL);
            free_call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            views.setOnClickPendingIntent(R.id.button_free_call, PendingIntent.getActivity(context, 0, free_call, 0));

            Intent unknown_call = new Intent(context, WidgetContainerActivity.class);
            unknown_call.putExtra(WidgetContainerActivity.FRAGMENT_INDEX, WidgetContainerActivity.UNKNOWN_CALL);
            unknown_call.setAction(Utils.ACTION_UNKNOWN_CALL);
            unknown_call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            views.setOnClickPendingIntent(R.id.button_private, PendingIntent.getActivity(context, 0, unknown_call, 0));

            String credit = mSharedPreferences.getCreditConsult(context);
            Uri uri = Uri.parse("tel:" + Uri.encode(credit));
            free_call = new Intent(Intent.ACTION_CALL);
            free_call.setData(uri);
            views.setOnClickPendingIntent(R.id.button_credit, PendingIntent.getActivity(context, 0, free_call, 0));
            free_call.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            Intent transfer = new Intent(context, WidgetContainerActivity.class);
            transfer.putExtra(WidgetContainerActivity.FRAGMENT_INDEX, WidgetContainerActivity.TRANSFER);
            transfer.setAction(Utils.ACTION_TRANSFER);
            transfer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            views.setOnClickPendingIntent(R.id.button_transfer, PendingIntent.getActivity(context, 0, transfer, 0));

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

}
