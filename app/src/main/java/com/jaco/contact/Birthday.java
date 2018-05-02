package com.jaco.contact;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.jaco.contact.preferences.mSharedPreferences;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Created by osvel on 7/28/16.
 */
public class Birthday {

    public static final int PLUS_TIME = 18000000;
    public static final String CUSTOM_NUMBER = "13579246800864297531";

    public static final String ACTION = "com.jaco.contact.BIRTHDAY_SCHEDULE";
    public static final String PHONE_NUMBER = "phone_number";

    private static final String DATE_FORMAT = "D d M yyyy";

    private ContactDetail contact_detail;
    private String owner_name;
    private GregorianCalendar birthday;
    private GregorianCalendar custom;
    private boolean ignored;
    private boolean valid;
    private String image_uri;

    public Birthday(String contact_name, GregorianCalendar custom){
        this.contact_detail = new ContactDetail(contact_name, CUSTOM_NUMBER);
        this.owner_name = "";
        this.ignored = false;
        this.image_uri = "";
        this.valid = true;
        this.birthday = custom;
        this.custom = custom;
    }

    public Birthday(String contact_name, String contact_number, String owner_name, String time, String custom_time, String image_uri, boolean ignored){
        this.contact_detail = new ContactDetail(contact_name, contact_number);
        this.owner_name = owner_name;
        this.ignored = ignored;
        this.image_uri = image_uri;
        valid = true;

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        try {
            Date parsed = df.parse(time);
            birthday = new GregorianCalendar();
            birthday.setTime(parsed);
        } catch (ParseException e) {
            e.printStackTrace();
            birthday = null;
        }

        try {
            Date parsed = df.parse(custom_time);
            custom = new GregorianCalendar();
            custom.setTime(parsed);
        } catch (ParseException e) {
            e.printStackTrace();
            custom = null;
        }
    }

    public Birthday(Context context, ContactDetail contact_detail) {
        this.contact_detail = contact_detail;
        this.ignored = false;
        this.custom = null;
        valid = true;

        EtecsaDB db = new EtecsaDB(context);
        String number = contact_detail.getNumber();
        PhoneNumber phoneNumber = new PhoneNumber(number);
        PhoneEntry[] entry = null;
        //si es movil buscar en la base de datos
        if (phoneNumber.isMovil()){
            entry = db.searchByNumber(number);
        }

        image_uri = Contacts.getInstance(context).getUriDisplayPhoto(number).toString();

        GregorianCalendar current = new GregorianCalendar();
        if (entry != null && entry.length > 0){
            //si se encontro algo establecer los datos
            owner_name = entry[0].getName();
            birthday = entry[0].getBirthDate();
            if (birthday == null)
                valid = false;
            else{
                valid = true;
                birthday.set(Calendar.YEAR, current.get(Calendar.YEAR));
            }
        }
        else {
            //si no se encontro nada se pone en null
            owner_name = null;
            birthday = null;
            valid = false;
        }
        db.close();
        setBirthday(current);
    }

    public void setCustom(GregorianCalendar custom){
        this.custom = custom;
    }

    public boolean isValid() {
        return valid;
    }

    public String getImageUri() {
        return image_uri;
    }

    public int getId(){
        PhoneNumber phoneNumber = new PhoneNumber(getContactNumber());
        String number = phoneNumber.getNumber();
        try {
            return Integer.parseInt(number);
        }
        catch (NumberFormatException e){
            int id = 0;
            String cadena = number.length() == 0 ? getContactName() : number;
            for (int i = 0; i < cadena.length(); i++) {
                id += cadena.charAt(i)*i;
            }
            return id;
        }
    }

    private void setBirthday(GregorianCalendar current){

        if (custom != null)
            custom.set(Calendar.YEAR, current.get(Calendar.YEAR));
        if (birthday != null)
            birthday.set(Calendar.YEAR, current.get(Calendar.YEAR));

        if (custom != null && custom.getTimeInMillis() < current.getTimeInMillis()){
            //si hay compleaños y ya paso se establece para el año siguiente y se actualiza en la base de datos
            custom.set(Calendar.YEAR, current.get(Calendar.YEAR)+1);
        }

        if (birthday != null && birthday.getTimeInMillis() < current.getTimeInMillis()){
            //si hay compleaños y ya paso se establece para el año siguiente y se actualiza en la base de datos
            birthday.set(Calendar.YEAR, current.get(Calendar.YEAR)+1);
        }

        DateFormat df = new SimpleDateFormat("yyyy LLL dd");
    }

    public String getContactName() {
        return contact_detail.getName();
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public String getContactNumber() {
        return contact_detail.getNumber();
    }

    public String getOwnerName() {
        return owner_name;
    }

    public GregorianCalendar getBirthday() {

        GregorianCalendar current = new GregorianCalendar();
        setBirthday(current);

        if (custom != null)
            return custom;

        return birthday;
    }

    public boolean isCustom(){
        return custom != null;
    }

    public String getStringBirthday(){

        GregorianCalendar current = new GregorianCalendar();
        setBirthday(current);
        DateFormat df = new SimpleDateFormat("LLL dd");

        if (custom != null){
            return df.format(custom.getTime());
        }

        if (birthday != null){
            return df.format(birthday.getTime());
        }

        return null;
    }

    public String getLeftTime(Context context){

        if (birthday == null && custom == null)
            return null;

        GregorianCalendar calendar = new GregorianCalendar();
        setBirthday(calendar);

        long left;
        if (custom != null)
            left = custom.getTimeInMillis() - calendar.getTimeInMillis();
        else
            left = birthday.getTimeInMillis() - calendar.getTimeInMillis();

        left += PLUS_TIME;
        calendar.setTimeInMillis(left);

        if (calendar.get(Calendar.MONTH) != 0){
            return calendar.get(Calendar.MONTH) + " " + context.getResources().getString(R.string.months);
        }

        return calendar.get(Calendar.DAY_OF_MONTH) + " " + context.getResources().getString(R.string.days);

    }

    public String getBirthdayForDatabase(){

        if (birthday == null)
            return "";

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(birthday.getTime());

    }

    public String getCustomForDatabase(){

        if (custom == null)
            return "";

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(custom.getTime());

    }

    public boolean notificateToday(Context context){

        int days_before = mSharedPreferences.getBirthdayNotificationDialog(context);
        GregorianCalendar birthday = getBirthday();
        GregorianCalendar current = new GregorianCalendar();
        birthday.set(Calendar.YEAR, current.get(Calendar.YEAR));
        //set time con los dias antes de la notificacion
        birthday.setTimeInMillis(birthday.getTimeInMillis() - (days_before*24*60*60*1000));

        String format = "D d M";
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(birthday.getTime()).equals(df.format(current.getTime()));

    }

    public void setSchedule(Context context) {

        int days_before = mSharedPreferences.getBirthdayNotificationDialog(context);
        setSchedule(context, days_before);

    }

    public void setSchedule(Context context, int days_before) {

        GregorianCalendar current = new GregorianCalendar();
        setBirthday(current);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ACTION);
        intent.putExtra(PHONE_NUMBER, getContactNumber());
        intent.setPackage(context.getPackageName());

        long millis = 0;
//        if (!ignored && custom != null){
        if (custom != null){
            millis = custom.getTimeInMillis()-(days_before*24*60*60*1000);
//            manager.set(AlarmManager.RTC_WAKEUP, custom.getTimeInMillis()-(days_before*24*60*60*1000), PendingIntent.getBroadcast(context, getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }
//        else if (!ignored && birthday != null){
        else if (birthday != null){
            millis = birthday.getTimeInMillis()-(days_before*24*60*60*1000);
//            manager.set(AlarmManager.RTC_WAKEUP, birthday.getTimeInMillis()-(days_before*24*60*60*1000), PendingIntent.getBroadcast(context, getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }

        //si la notificacion ya paso se establece para el siguiente año
        if (current.getTimeInMillis() > millis){
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(millis);
            calendar.set(Calendar.YEAR, current.get(Calendar.YEAR) + 1);
            millis = calendar.getTimeInMillis();
        }

        if (custom != null || birthday != null) {
            manager.set(AlarmManager.RTC_WAKEUP, millis, PendingIntent.getBroadcast(context, getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT));
        }

    }

    @Override
    public String toString() {
        return "Contact: "+getContactName()+" " +
                "Owner: "+owner_name+" " +
                "Number: "+getContactNumber()+" " +
                "Birthday: "+getStringBirthday();
    }
}
