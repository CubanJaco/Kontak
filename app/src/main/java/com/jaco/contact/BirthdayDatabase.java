package com.jaco.contact;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 7/28/16.
 */
public class BirthdayDatabase {

    private static final int IGNORED_TRUE = 1;
    private static final int IGNORED_FALSE = 0;

    private static final String DATABASE_NAME = "birthdays_database";
    private static final String TABLE_BIRTHDAY = "birthdays";
    private static final String BIRTHDAY_ID = "_id";
    private static final String BIRTHDAY_NAME = "name";
    private static final String BIRTHDAY_OWNER = "owner";
    private static final String BIRTHDAY_NUMBER = "number";
    private static final String BIRTHDAY_BIRTHDAY = "birthday";
    private static final String BIRTHDAY_IGNORED = "ignored";
    private static final String BIRTHDAY_IMAGE_URI = "image_uri";
    private static final String BIRTHDAY_CUSTOM_BIRTHDAY = "custom";

    private static final int BIRTHDAY_NAME_INDEX = 1;
    private static final int BIRTHDAY_OWNER_INDEX = 2;
    private static final int BIRTHDAY_NUMBER_INDEX = 3;
    private static final int BIRTHDAY_BIRTHDAY_INDEX = 4;
    private static final int BIRTHDAY_IGNORED_INDEX = 5;
    private static final int BIRTHDAY_IMAGE_URI_INDEX = 6;
    private static final int BIRTHDAY_CUSTOM_BIRTHDAY_INDEX = 7;


    private mOpenHelper mOpenHelper;

    public BirthdayDatabase(Context context) {
        mOpenHelper = new mOpenHelper(context, DATABASE_NAME, null, 1);
    }

    public boolean syncBirthday(Birthday birthday){

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(BIRTHDAY_NAME, birthday.getContactName());
        newValues.put(BIRTHDAY_OWNER, birthday.getOwnerName());
        newValues.put(BIRTHDAY_BIRTHDAY, birthday.getBirthdayForDatabase());
        newValues.put(BIRTHDAY_IMAGE_URI, birthday.getImageUri());

        int updated = db.update(TABLE_BIRTHDAY, newValues, BIRTHDAY_NUMBER + "=?", new String[]{birthday.getContactNumber()});
        db.close();

        if (updated == -1 || updated == 0){
            return addBirthday(birthday);
        }

        return updated != -1 && updated != 0;

    }

    public boolean addBirthday(Birthday birthday){

        if (!birthday.isValid())
            return false;

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        ContentValues newValues = new ContentValues();
        newValues.put(BIRTHDAY_NAME, birthday.getContactName());
        newValues.put(BIRTHDAY_OWNER, birthday.getOwnerName());
        newValues.put(BIRTHDAY_NUMBER, birthday.getContactNumber());
        newValues.put(BIRTHDAY_BIRTHDAY, birthday.getBirthdayForDatabase());
        newValues.put(BIRTHDAY_IGNORED, IGNORED_FALSE);
        newValues.put(BIRTHDAY_IMAGE_URI, birthday.getImageUri());
        newValues.put(BIRTHDAY_CUSTOM_BIRTHDAY, birthday.getCustomForDatabase());

        int insert = (int) db.insert(TABLE_BIRTHDAY, null, newValues);
        db.close();

        return insert != -1;

    }

    public boolean setIgnored(Birthday birthday){

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(BIRTHDAY_IGNORED, birthday.isIgnored() ? IGNORED_TRUE : IGNORED_FALSE);

        int insert = db.update(TABLE_BIRTHDAY, newValues, BIRTHDAY_NUMBER + "=?", new String[]{birthday.getContactNumber()});
        db.close();

        return insert != -1;

    }

    public boolean setCustomBirthday(Birthday birthday){

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ContentValues newValues = new ContentValues();
        newValues.put(BIRTHDAY_CUSTOM_BIRTHDAY, birthday.getCustomForDatabase());

        int insert = db.update(TABLE_BIRTHDAY, newValues, BIRTHDAY_NUMBER + "=?", new String[]{birthday.getContactNumber()});
        db.close();

        return insert != -1;

    }

    public boolean delete(Birthday birthday) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int remove = db.delete(TABLE_BIRTHDAY, BIRTHDAY_NUMBER + "=? AND "
                +BIRTHDAY_NAME+"=?",
                new String[]{birthday.getContactNumber(),
                    birthday.getContactName()}
        );
        db.close();

        return remove != 0;

    }

    public List<Birthday> getAllBirthdays(boolean show_ignore){

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor query = db.query(true, TABLE_BIRTHDAY, null, show_ignore ? "" : BIRTHDAY_IGNORED+"="+IGNORED_FALSE, null, null, null, null, null);

        if (query == null){
            db.close();
            return new ArrayList<>();
        }

        if (query.getCount() == 0) {
            query.close();
            db.close();
            return new ArrayList<>();
        }

        List<Birthday> birthdays = new ArrayList<>();
        while (query.moveToNext()){
            String name = query.getString(BIRTHDAY_NAME_INDEX);
            String number = query.getString(BIRTHDAY_NUMBER_INDEX);
            String owner = query.getString(BIRTHDAY_OWNER_INDEX);
            String birthday = query.getString(BIRTHDAY_BIRTHDAY_INDEX);
            int ignored = query.getInt(BIRTHDAY_IGNORED_INDEX);
            String image_uri = query.getString(BIRTHDAY_IMAGE_URI_INDEX);
            String custom = query.getString(BIRTHDAY_CUSTOM_BIRTHDAY_INDEX);

            birthdays.add(new Birthday(name, number, owner, birthday, custom, image_uri, ignored == IGNORED_TRUE));
        }
        query.close();
        db.close();

        return birthdays;
    }

    public Birthday getBirthday(String number){

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor query = db.query(true, TABLE_BIRTHDAY, null, BIRTHDAY_NUMBER + "=?", new String[]{number}, null, null, null, null);

        if (query == null){
            db.close();
            return null;
        }

        if (query.getCount() == 0) {
            query.close();
            db.close();
            return null;
        }

        if (query.moveToFirst()){
            String name = query.getString(BIRTHDAY_NAME_INDEX);
            String owner = query.getString(BIRTHDAY_OWNER_INDEX);
            String birthday = query.getString(BIRTHDAY_BIRTHDAY_INDEX);
            int ignored = query.getInt(BIRTHDAY_IGNORED_INDEX);
            String image_uri = query.getString(BIRTHDAY_IMAGE_URI_INDEX);
            String custom = query.getString(BIRTHDAY_CUSTOM_BIRTHDAY_INDEX);

            query.close();
            db.close();

            return new Birthday(name, number, owner, birthday, custom, image_uri, ignored == IGNORED_TRUE);
        }
        query.close();
        db.close();

        return null;
    }

    public static void createTables(SQLiteDatabase db){
        db.execSQL("CREATE TABLE \""+TABLE_BIRTHDAY+"\" (" +
                "\""+BIRTHDAY_ID+"\" INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "\""+BIRTHDAY_NAME+"\" TEXT, " +
                "\""+BIRTHDAY_OWNER+"\" TEXT, " +
                "\""+BIRTHDAY_NUMBER+"\" TEXT NOT NULL, " +
                "\""+BIRTHDAY_BIRTHDAY+"\" TEXT, " +
                "\""+BIRTHDAY_IGNORED+"\" INTEGER, " +
                "\""+BIRTHDAY_IMAGE_URI+"\" TEXT, " +
                "\""+BIRTHDAY_CUSTOM_BIRTHDAY+"\" TEXT);");
    }

    public static void dropTables(SQLiteDatabase db){
        String sql = "DROP TABLE IF EXISTS \""+TABLE_BIRTHDAY+"\"";
        db.execSQL(sql);
    }

    public static class mOpenHelper extends SQLiteOpenHelper{

        public mOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public mOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion != oldVersion) {
                dropTables(db);
                createTables(db);
            }
        }
    }

}
