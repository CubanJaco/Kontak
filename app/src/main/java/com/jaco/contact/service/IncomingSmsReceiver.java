package com.jaco.contact.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import com.jaco.contact.Contacts;
import com.jaco.contact.EtecsaDB;
import com.jaco.contact.PhoneEntry;
import com.jaco.contact.R;
import com.jaco.contact.Utils;
import com.jaco.contact.preferences.mSharedPreferences;

import java.io.InputStream;

/**
 * Created by osvel on 7/23/16.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {

    private static final Uri MMS_SMS_CONTENT_URI = Uri.parse("content://mms-sms/");
    private static final Uri THREAD_ID_CONTENT_URI = Uri.withAppendedPath(MMS_SMS_CONTENT_URI, "threadID");

    private static final String TAG = "IncomingSmsReceiver";

    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (Object pdus : pdusObj) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdus);

                    String senderNum = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    //calcular el id de la notificacion
                    int id = 0;
                    for (int i = 0; i < senderNum.length(); i++)
                        id += senderNum.charAt(i)*i;

                    int notification = mSharedPreferences.getSmsNotification(context);
                    switch (notification){
                        case 0: {
                            notification(context, id, senderNum, message);
                            break;
                        }
                        case 1: {
                            Contacts contact = Contacts.getInstance(context);
                            String name = contact.getNameByNumber(senderNum);
                            if (name == null)
                                notification(context, id, senderNum, message);
                        }
                    }

                    Log.i(TAG, "senderNum: " + senderNum + "; message: " + message);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception smsReceiver" + e);

        }
    }

    public static void notification(Context context, int id, String senderNum, String message) {

        Intent sms_thread = new Intent(Intent.ACTION_VIEW);
        sms_thread.setData(Uri.parse("content://mms-sms/conversations/" + IncomingSmsReceiver.findThreadIdFromAddress(context, senderNum)));

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            color = context.getResources().getColor(R.color.colorAccent, context.getTheme());
        else
            color = context.getResources().getColor(R.color.colorAccent);

        Contacts contacts = Contacts.getInstance(context);
        String contact_id = contacts.getIdByNumber(senderNum);
        String contact_name = contacts.getNameByNumber(senderNum);
        EtecsaDB etecsaDB = new EtecsaDB(context);
        PhoneEntry[] entries = null;

        if (contact_id == null && etecsaDB.hasDatabase()){
            entries = etecsaDB.searchByNumber(senderNum);
        }

        //obtener contact_name desde la base de datos o poner desconocido
        if (entries != null){
            if (entries.length != 0)
                contact_name = entries[0].getName();
            else
                contact_name = senderNum;
        }
        else if (contact_id == null)
            contact_name = senderNum;

        //get inputStream
        InputStream inputStream = null;
        if (contact_id != null)
            inputStream = contacts.openDisplayPhoto(Long.parseLong(contact_id));

        //get bitmap by inputStream
        Bitmap image = null;
        if (inputStream != null)
            image = BitmapFactory.decodeStream(inputStream);

        //get bitmap if inputStream is invalid
        if (image == null)
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.user);

        String channel = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = Utils.createChannel(context, context.getString(R.string.call_sms_notification), Utils.NOTIFICATION_CHANEL_CALL_MESSAGE_ID,
                    NotificationManager.IMPORTANCE_DEFAULT, false, true);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channel)
                        .setSmallIcon(R.drawable.message_notification_icon)
                        .setLargeIcon(image)
                        .setContentTitle(contact_name)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message))
                        .setContentIntent(PendingIntent.getActivity(context, id, sms_thread, PendingIntent.FLAG_CANCEL_CURRENT))
                        .setColor(color)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                        .setAutoCancel(true);

        // Construir la notificación y emitirla
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }


    public static long findThreadIdFromAddress(Context context, String address) {
        if (address == null)
            return 0;

        String THREAD_RECIPIENT_QUERY = "recipient";

        Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
        uriBuilder.appendQueryParameter(THREAD_RECIPIENT_QUERY, address);

        long threadId = 0;

        Cursor cursor = null;
        try {

            cursor = context.getContentResolver().query(
                    uriBuilder.build(),
                    new String[] { ContactsContract.Contacts._ID },
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                threadId = cursor.getLong(0);
            }
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return threadId;
    }
}
