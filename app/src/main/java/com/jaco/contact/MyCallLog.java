package com.jaco.contact;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by osvel on 7/27/16.
 */
public class MyCallLog {

    private Context context;

    public MyCallLog(Context context) {
        this.context = context;
    }

    /**
     *
     * @return Call object if last call was missed or null if was not
     */
    public Call lastCallIsMissed(){

        Uri uri = CallLog.Calls.CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, null, null, null, CallLog.Calls.DATE + " DESC LIMIT 1");
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

        if (cursor != null && cursor.moveToFirst()){
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            long date = Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
            int type = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
            int duration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));

            cursor.close();

            return type == CallLog.Calls.MISSED_TYPE ? new Call(number, date, type, duration) : null;
        }

        return null;

    }

    public List<Call> getMissesCalls(){

        Uri uri = CallLog.Calls.CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    uri,
                    null,
                    CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + " = 1",
                    null,
                    CallLog.Calls.DATE + " DESC"
                );
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

        List<Call> calls = new ArrayList<>();

        if (cursor == null)
            return calls;

        while (cursor.moveToNext()){

            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            long date = Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
            int type = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
            int duration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));

            calls.add(new Call(number, date, type, duration));

        }

        cursor.close();

        return calls;
    }

    public List<Call> getCallLog(){
        return getCallLog(1);
    }

    public List<Call> getCallLog(int page){

        List<MyCallLog.Call> calls = new ArrayList<>();

        int pageEnd = 25;
        int pageStart = (page - 1) * pageEnd;

        Uri uri = CallLog.Calls.CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, null, null, null, CallLog.Calls.DATE + " DESC LIMIT "+pageEnd+" OFFSET "+pageStart);
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }

        if (cursor != null && cursor.getCount() > 0){
            while (cursor.moveToNext()){
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                long date = Long.parseLong(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
                int type = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
                int duration = Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
                calls.add(new Call(number, date, type, duration));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return calls;
    }

    public class Call{

        protected String number;
        protected GregorianCalendar date;
        protected int type;
        protected int duration;

        public Call(String number, long date, int type, int duration) {
            this.number = number;
            this.date = new GregorianCalendar();
            this.date.setTimeInMillis(date);
            this.type = type;
            this.duration = duration;
        }

        public String getNumber(){
            return number;
        }

        public String getDay(){

            Calendar today = new GregorianCalendar();
            if (date.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
                    && date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && date.get(Calendar.YEAR) == today.get(Calendar.YEAR))
                return context.getResources().getString(R.string.today);

            Calendar yesterday = new GregorianCalendar();
            yesterday.setTimeInMillis(today.getTimeInMillis() - (24*60*60*1000));
            if (date.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH)
                    && date.get(Calendar.MONTH) == yesterday.get(Calendar.MONTH)
                    && date.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR))
                return context.getResources().getString(R.string.yesterday);

            String dateFormat = "dd '"+context.getResources().getString(R.string.of)+"' LLL";
            DateFormat df = new SimpleDateFormat(dateFormat);

            return df.format(date.getTime());

        }

        public String getStringTime(){

            DateFormat df = new SimpleDateFormat("KK:mm a");
            return df.format(date.getTime());

        }

        public GregorianCalendar getDate() {
            return date;
        }

        public void setDate(GregorianCalendar date) {
            this.date = date;
        }

        public int getType(){
            return type;
        }

        public String getDuration(){

            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(duration * 1000);
            DateFormat df;

            if (duration < 60)
                df = new SimpleDateFormat("s 'seg.'");
            else if (duration < 60*60)
                df = new SimpleDateFormat("m:ss 'min.'");
            else {
                df = new SimpleDateFormat(":mm:ss 'h.'");
                int hours = duration / (60*60);
                return hours+df.format(calendar.getTime());
            }

            return df.format(calendar.getTime());
        }
    }
}
