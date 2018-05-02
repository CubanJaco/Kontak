package com.jaco.contact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaco.contact.preferences.mSharedPreferences;

import java.sql.SQLException;

/**
 * Created by osvel on 6/29/16.
 */
public class AlertActivity extends Activity {

    public final static String CLOSE = "CLOSE";
    public final static String PHONE = "PHONE_NUMBER";

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null)
            phoneNumber = getIntent().getStringExtra(PHONE);
        else
            phoneNumber = "";

        //si el numero esta vacio cierro la alerta
        if (phoneNumber == null || phoneNumber.length() == 0) {
            finish();
            return;
        }

        //verificar si se muestra o no la alerta
        int showAlert = mSharedPreferences.getShowAlert(this);
        PhoneNumber phone;

        switch (showAlert){
            case 0: {
                //mostrar notificacion siempre
                showAlert(phoneNumber);
                break;
            }
            case 1: {
                //mostrar notificacion solo con *99
                phone = new PhoneNumber(phoneNumber);
                if (phone.is99())
                    showAlert(phoneNumber);
                else
                    finish();
                break;
            }
            case 2: {
                //mostrar notificacion solo de numeros desconocidos
                //**llamadas con *99 de contactos registrados no se notifican**
                phone = new PhoneNumber(phoneNumber);
                String id = Contacts.getInstance(this).getIdByNumber(phone.getNumber());
                if (id != null)
                    finish();
                else
                    showAlert(phoneNumber);
                break;
            }
            case 3: {
                //mostrar notificacion de numeros desconocidos y llamadas con *99
                phone = new PhoneNumber(phoneNumber);
                String id = Contacts.getInstance(this).getIdByNumber(phone.getNumber());
                if (id == null || phone.is99()){
                    showAlert(phoneNumber);
                }
                else
                    finish();
                break;
            }
            default:
                finish();
        }

    }

    private void showAlert(String phoneNumber){

        Window thisWindow = getWindow();
        thisWindow.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        thisWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        thisWindow.setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);

//        thisWindow.setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE | WindowManager.LayoutParams.TYPE_PHONE);

        //obtener la posicion definida por el usuario
        int gravity = mSharedPreferences.getAlertPosition(this);
        switch (gravity){
            case 0: {
                thisWindow.setGravity(Gravity.TOP);
                break;
            }
            case 1: {
                thisWindow.setGravity(Gravity.CENTER);
                break;
            }
            case 2: {
                thisWindow.setGravity(Gravity.BOTTOM);
                break;
            }
        }

        setContentView(R.layout.activity_notify);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(thisWindow.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        thisWindow.setAttributes(lp);

        PhoneNumber phone = new PhoneNumber(this.phoneNumber);

        if (!phone.isFree()){
            LinearLayout alert_layout = (LinearLayout) findViewById(R.id.notification_dialog);
            alert_layout.setBackgroundResource(R.color.dialog_Red);
        }

        Contacts contact = Contacts.getInstance(this);
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

        ImageView imageView = (ImageView) findViewById(R.id.notification_picture);
        if (bmp != null)
            imageView.setImageBitmap(bmp);
        else
            imageView.setImageResource(R.drawable.user);

        TextView nameTextView = (TextView) findViewById(R.id.notification_name);
        TextView phoneTextView = (TextView) findViewById(R.id.notification_number);
        TextView addressTextView = (TextView) findViewById(R.id.notification_address);
        TextView provinceTextView = (TextView) findViewById(R.id.notification_province);
        if (contactName != null){
            nameTextView.setText(contactName);
            phoneTextView.setText(phone.getNumber());
            addressTextView.setVisibility(View.GONE);
            provinceTextView.setVisibility(View.GONE);
        }
        else if (phoneNumber.length() != 0){

            EtecsaDB db = new EtecsaDB(this);

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
                nameTextView.setText(getResources().getString(R.string.unknown));
                phoneTextView.setText(phone.getNumber());
                addressTextView.setVisibility(View.GONE);
            }

            if (currentPhone != null && currentPhone.getType() == PhoneType.FIX)
                provinceTextView.setText(currentPhone.getProvinceName(this));
            else
                provinceTextView.setVisibility(View.GONE);


        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getExtras().getBoolean(CLOSE)) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        finish();
    }

}
