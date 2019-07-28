package com.jaco.contact.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaco.contact.Contacts;
import com.jaco.contact.EtecsaDB;
import com.jaco.contact.FlashController;
import com.jaco.contact.MyCallLog;
import com.jaco.contact.PhoneEntry;
import com.jaco.contact.PhoneNumber;
import com.jaco.contact.PhoneType;
import com.jaco.contact.ProfileActivity;
import com.jaco.contact.R;
import com.jaco.contact.Utils;
import com.jaco.contact.preferences.mSharedPreferences;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

public class MPhone {

    private Context context;
    private boolean outgoing;
    private View alert;

    protected static final String ACTION_PHONE_STATE = "android.intent.action.PHONE_STATE";
    protected static final String IDLE = "IDLE";
    protected static final String OFFHOOK = "OFFHOOK";
    protected static final String RINGING = "RINGING";

    public MPhone(Context context, boolean outgoing) {
        this.context = context;
        this.outgoing = outgoing;
    }

    public View getAlert() {
        return alert;
    }

    public void setAlert(View alert) {
        this.alert = alert;
    }

    public void handlePhoneState(int state, final String incomingNumber) {

        String privState = IDLE;

        if (state == TelephonyManager.CALL_STATE_RINGING)
            privState = RINGING;
        else if (state == TelephonyManager.CALL_STATE_OFFHOOK)
            privState = OFFHOOK;

        handlePhoneState(privState, incomingNumber);

    }

    public void handlePhoneState(String state, final String incomingNumber) {

        switch (state){
            case RINGING: {
                if (!outgoing) {
                    showDialog(incomingNumber);
                    startFlash(incomingNumber);
                }
                break;
            }
            case OFFHOOK: {
                if (!outgoing) {
                    closeDialog();
                    stopFlash();
                }
                break;
            }
            default: {
                if (!outgoing){
                    closeDialog();
                    stopFlash();
                }

                outgoing = false;
                //detener la notificacion por 2 segundos para dar tiempo al telefono a guardar la llamada en el registro de llamadas
                new Handler().postDelayed(new Runnable() {
                                              public void run() {
                                                  notifyCall(context, incomingNumber);
                                              }
                                          }, 2000
                );
                break;
            }
        }
    }


    public void stopFlash(){
        //stop flashing recursive
        stopFlashRecursive(10);
    }

    public void stopFlashRecursive(final int i){

        //stop flashing
        FlashController.getInstance().stopFlashing();

        if (i > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopFlashRecursive(i - 1);
                }
            }, 100);
        }

    }

    public void startFlash(String phoneNumber){

        PhoneNumber phone = new PhoneNumber(phoneNumber);
        int flashAlert = mSharedPreferences.getFlashAlert(context);
        switch (flashAlert){
            case 0: {
                //siempre
                //start flashing
                FlashController.getInstance().startFlashing(context);
                break;
            }
            case 1: {
                //gratis
                if (phone.isFree()){
                    //start flashing
                    FlashController.getInstance().startFlashing(context);
                }
                break;
            }
            case 2: {
                //con costo
                if (!phone.isFree()){
                    //start flashing
                    FlashController.getInstance().startFlashing(context);
                }
                break;
            }
        }
    }

    public void showDialog(String phoneNumber){

        //verificar si se muestra o no la alerta
        int showAlert = mSharedPreferences.getShowAlert(context);
        PhoneNumber phone = new PhoneNumber(phoneNumber);
        Contacts contacts = Contacts.getInstance(context);

        switch (showAlert) {
            case 0: {
                //notificar siempre
                startDialog(phoneNumber);
                break;
            }
            case 1: {
                //notificar solo con 99
                if (phone.is99())
                    startDialog(phoneNumber);
                break;
            }
            case 2: {
                //notificar solo desconocidas
                if (contacts.getNameByNumber(phone.getNumber()) == null)
                    startDialog(phoneNumber);
                break;
            }
            case 3: {
                //notificar desconocidas && 99
                if (phone.is99() || contacts.getNameByNumber(phone.getNumber()) == null)
                    startDialog(phoneNumber);
                break;
            }
        }

    }

    public void startDialog(String phoneNumber){

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        try {
            if (alert != null){
                wm.removeViewImmediate(alert);
            }
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        alert = inflater.inflate(R.layout.activity_notify, null);

        int type;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT){
            type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        //obtener la posicion definida por el usuario
        int gravity = mSharedPreferences.getAlertPosition(context);
        switch (gravity){
            case 0: {
                params.gravity = Gravity.TOP;
                break;
            }
            case 1: {
                params.gravity = Gravity.CENTER;
                break;
            }
            case 2: {
                params.gravity = Gravity.BOTTOM;
                break;
            }
        }

        PhoneNumber phone = new PhoneNumber(phoneNumber);

        if (!phone.isFree()){
            LinearLayout alert_layout = (LinearLayout) alert.findViewById(R.id.notification_dialog);
            alert_layout.setBackgroundResource(R.color.dialog_Red);
        }

        Contacts contact = Contacts.getInstance(context);
        String contactName = contact.getNameByNumber(phone.getNumber());
        String contactId = contact.getIdByNumber(phone.getNumber());
        Bitmap bmp;
        try {
            bmp = contact.getThumbnailPhoto(Long.parseLong(contactId));
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            bmp = null;
        }

        ImageView imageView = (ImageView) alert.findViewById(R.id.notification_picture);
        if (bmp != null)
            imageView.setImageBitmap(bmp);
        else
            imageView.setImageResource(R.drawable.user);

        TextView nameTextView = (TextView) alert.findViewById(R.id.notification_name);
        TextView phoneTextView = (TextView) alert.findViewById(R.id.notification_number);
        TextView addressTextView = (TextView) alert.findViewById(R.id.notification_address);
        TextView provinceTextView = (TextView) alert.findViewById(R.id.notification_province);
        if (contactName != null){
            nameTextView.setText(contactName);
            phoneTextView.setText(phone.getNumber());
            addressTextView.setVisibility(View.GONE);
            provinceTextView.setVisibility(View.GONE);
        }
        else if (phoneNumber.length() != 0){

            EtecsaDB db = new EtecsaDB(context);

            try {
                db.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            PhoneEntry currentPhone = null;
            if (db.isOpen()){
                PhoneEntry[] phones = db.searchByNumber(phoneNumber);
                currentPhone = (phones != null && phones.length > 0) ? phones[0] : null;
            }

            if (currentPhone != null){
                nameTextView.setText(currentPhone.getName());
                phoneTextView.setText(phone.getNumber());
                addressTextView.setText(currentPhone.getAddress());
            }
            else {
                nameTextView.setText(context.getResources().getString(R.string.unknown));
                phoneTextView.setText(phone.getNumber());
                addressTextView.setVisibility(View.GONE);
            }

            if (currentPhone != null && currentPhone.getType() == PhoneType.FIX)
                provinceTextView.setText(currentPhone.getProvinceName(context));
            else
                provinceTextView.setVisibility(View.GONE);


        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            wm.addView(alert, params);
        else if (Settings.canDrawOverlays(context)) {
            wm.addView(alert, params);
        }

    }

    public void closeDialog(){
        //intentar cerrar el dialogo 5 veces
        closeDialogRecursive(5);
    }

    public static void notifyCall(Context context, String incomingNumber) {

        int missedCallNotification = mSharedPreferences.getMissedCallNotification(context);

        MyCallLog.Call missedCall = new MyCallLog(context).lastCallIsMissed();

        //si la ultima llamada del registro de llamada es una llamada perdida y coincide con la llamada que se analiza
        missedCall = missedCall != null && incomingNumber != null && missedCall.getNumber().equals(incomingNumber)
                ? missedCall : null;

        /**
         * si no es una llamada perdida
         * && el numero entrante no es null
         * && el numero entrante es distinto de ""
         * && el numero entrante coincide con el de la llamada perdida
         */

        if (missedCall == null)
            return;

        PhoneNumber phoneNumber = new PhoneNumber(incomingNumber);
        Contacts contacts = Contacts.getInstance(context);

        switch (missedCallNotification) {
            case 0: {
                //notificar siempre
                notification(context, incomingNumber);
                break;
            }
            case 1: {
                //notificar solo con 99
                if (phoneNumber.is99()){
                    notification(context, incomingNumber);
                }
                break;
            }
            case 2: {
                //notificar solo desconocidas
                if (contacts.getNameByNumber(phoneNumber.getNumber()) == null){
                    notification(context, incomingNumber);
                }
                break;
            }
            case 3: {
                //notificar desconocidas && 99
                if (phoneNumber.is99() || contacts.getNameByNumber(phoneNumber.getNumber()) == null){
                    notification(context, incomingNumber);
                }
                break;
            }
        }

    }

    /**
     * Define how many times you want to try close alert dialog
     * @param i tries
     */
    public void closeDialogRecursive(final int i){

        boolean fail = false;

        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.removeViewImmediate(alert);
            alert = null;
        }
        catch (IllegalArgumentException e){
            e.printStackTrace();
            fail = true;
        }

        //si fallo && el contador es menor o igual que 5 && la alerta no es null
        //entonces se reintenta cerrar en 2 segundos
        if (fail && i > 0 && alert != null){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeDialogRecursive(i-1);
                }
            }, 2000);
        }

    }

    public static void notification(Context context, String caller) {

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            color = context.getResources().getColor(R.color.colorAccent, context.getTheme());
        else
            color = context.getResources().getColor(R.color.colorAccent);

        //obtener solo el numero
        String number = new PhoneNumber(caller).getNumber();

        Contacts contacts = Contacts.getInstance(context);
        String contact_id = contacts.getIdByNumber(number);
        String contact_name = contacts.getNameByNumber(number);
        EtecsaDB etecsaDB = new EtecsaDB(context);
        PhoneEntry[] entries = null;

        if (etecsaDB.hasDatabase()) {
            entries = etecsaDB.searchByNumber(number);
        }

        //obtener contact_name desde la base de datos o poner desconocido
        if (entries != null && contact_id == null) {
            if (entries.length != 0)
                contact_name = entries[0].getName();
            else
                contact_name = caller;
        }

        if (contact_name == null || contact_name.length() == 0) {
            contact_name = context.getResources().getString(R.string.unknown);
        }

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

        //obtener las llamadas perdidas nuevas
        List<MyCallLog.Call> misedsCalls = new MyCallLog(context).getMissesCalls();
        int missesCount = 0;

        //contar las llamadas perdidas del mismo numero
        for (MyCallLog.Call call : misedsCalls) {
            if (PhoneNumber.isValidNumber(caller) && call.getNumber().contains(number))
                missesCount++;
        }

        String content;
        if (missesCount <= 1)
            content = String.format(context.getResources().getString(R.string.missed_call), contact_name);
        else
            content = String.format(context.getResources().getString(R.string.missed_call_manny), missesCount, contact_name);

        caller = caller == null || caller.length() == 0 ? context.getResources().getString(R.string.unknown) : caller;

        String channel = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = Utils.createChannel(context, context.getString(R.string.call_sms_notification), Utils.NOTIFICATION_CHANEL_CALL_MESSAGE_ID,
                    NotificationManager.IMPORTANCE_DEFAULT, false, true);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channel)
                        .setSmallIcon(R.drawable.call_notifiaction_icon)
                        .setLargeIcon(image)
                        .setContentTitle(caller)
                        .setContentText(contact_name)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(content))
                        .setColor(color)
                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
//                        .addAction(R.drawable.call_notifiaction_icon,
//                                "Normal", null)
//                        .addAction(R.drawable.message_notification_icon,
//                                "SMS", null)
//                        .addAction(R.drawable.icon_cake,
//                                "*99", null)
                        .setAutoCancel(true);

        //calcular el id de la notificacion
        int id = 0;
        for (int i = 0; i < number.length(); i++)
            id += number.charAt(i) * i;

        Intent intent = new Intent(context, ProfileActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (entries != null && entries.length > 0) {
            intent.putExtra(ProfileActivity.PHONE_ENTRY, entries[0]);
        } else {
            PhoneEntry entry = new PhoneEntry(number);
            intent.putExtra(ProfileActivity.PHONE_ENTRY, entry);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Construir la notificación y emitirla
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }
}
