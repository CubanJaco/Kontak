package com.jaco.contact;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.jaco.contact.preferences.mSharedPreferences;

import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by osvel on 7/29/16.
 */
public class BirthdayBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()){
            case Birthday.ACTION: {

                boolean showNotification = mSharedPreferences.showBirthdayNotification(context);
                String number = intent.getExtras().getString(Birthday.PHONE_NUMBER);
                BirthdayDatabase db = new BirthdayDatabase(context);
                Birthday birthday = db.getBirthday(number);

                if (birthday != null && !birthday.isIgnored() && showNotification)
                    notification(context, birthday);

                birthday.setSchedule(context);
                break;
            }
            case Intent.ACTION_BOOT_COMPLETED: {

                //verificar si ya se notifico hoy
                boolean notificate_today = mSharedPreferences.getNotificationToday(context);
                //obtener la preferencia de mostrar notificacion de cumpleaños
                boolean showNotification = mSharedPreferences.showBirthdayNotification(context);

                BirthdayDatabase database = new BirthdayDatabase(context);
                List<Birthday> birthdays = database.getAllBirthdays(true);

                for (Birthday bd : birthdays) {
                    // si el cumpleaños se debe notificar hoy
                    // && no se ha notificado hoy
                    // && las preferencias permiten notificar
                    // && el cumpleaños no esta ignorado
                    if (bd.notificateToday(context) && !notificate_today && showNotification && !bd.isIgnored())
                        notification(context, bd);
                    bd.setSchedule(context);
                }

                break;
            }
        }
    }

    public void notification(Context context, Birthday birthday) {

        mSharedPreferences.setNotificationToday(context, new GregorianCalendar());

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            color = context.getResources().getColor(R.color.colorAccent, context.getTheme());
        else
            color = context.getResources().getColor(R.color.colorAccent);

        String title = birthday.getContactName();
        String name;
        if (birthday.isCustom())
            name = birthday.getContactName();
        else
            name = birthday.getOwnerName();

        int days_before = mSharedPreferences.getBirthdayNotificationDialog(context);

        String content;
        if (days_before == 0)
            content = String.format(context.getResources().getString(R.string.birthday_today), name);
        else
            content = String.format(context.getResources().getString(R.string.birthday_coming), name, days_before);

        Contacts contacts = Contacts.getInstance(context);
        String contact_id = contacts.getIdByNumber(birthday.getContactNumber());

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

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.birthday_notification_icon)
                        .setLargeIcon(image)
                        .setContentTitle(title)
                        .setContentText(name)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(content))
                        .setColor(color)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setAutoCancel(true);

        boolean silence = mSharedPreferences.silenceBirthdayNotification(context);
        if (!silence  && notificationSound != null)
            builder.setSound(notificationSound, Notification.DEFAULT_SOUND);
        else if (!silence)
            builder.setSound(null, Notification.DEFAULT_SOUND);

        PhoneEntry[] entries;
        if (!birthday.isCustom()){
            PhoneNumber phoneNumber = new PhoneNumber(birthday.getContactNumber());
            EtecsaDB db = new EtecsaDB(context);
            entries = db.searchByNumber(phoneNumber.getNumber());
        }
        else
            entries = null;

        Intent intent = new Intent(context, ProfileActivity.class);
        if (entries != null && entries.length > 0){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(ProfileActivity.PHONE_ENTRY, entries[0]);
            builder.setContentIntent(PendingIntent.getActivity(context, birthday.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }

        // Construir la notificación y emitirla
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(birthday.getId(), builder.build());

    }
}
