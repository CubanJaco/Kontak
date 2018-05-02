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
public class WidgetCreditAll extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_credit_all);

            String credit = mSharedPreferences.getCreditConsult(context);
            Uri uri = Uri.parse("tel:" + Uri.encode(credit));
            Intent credit_intent = new Intent(Intent.ACTION_CALL);
            credit_intent.setData(uri);
            views.setOnClickPendingIntent(R.id.button_credit, PendingIntent.getActivity(context, 0, credit_intent, 0));
            credit_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            uri = Uri.parse("tel:" + Uri.encode("*222*266#"));
            credit_intent = new Intent(Intent.ACTION_CALL);
            credit_intent.setData(uri);
            views.setOnClickPendingIntent(R.id.button_saldo_bono, PendingIntent.getActivity(context, 0, credit_intent, 0));
            credit_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            uri = Uri.parse("tel:" + Uri.encode("*222*869#"));
            credit_intent = new Intent(Intent.ACTION_CALL);
            credit_intent.setData(uri);
            views.setOnClickPendingIntent(R.id.button_saldo_voz, PendingIntent.getActivity(context, 0, credit_intent, 0));
            credit_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            uri = Uri.parse("tel:" + Uri.encode("*222*767#"));
            credit_intent = new Intent(Intent.ACTION_CALL);
            credit_intent.setData(uri);
            views.setOnClickPendingIntent(R.id.button_saldo_sms, PendingIntent.getActivity(context, 0, credit_intent, 0));
            credit_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

}
