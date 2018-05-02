package com.jaco.contact;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 6/29/16.
 */
public class Contacts {

    private static Contacts contacts = null;

    private Context context;
    private List<ContactDetail> contactsDetails;

    public Contacts(Context context) {
        this.context = context;
        contactsDetails = null;
    }

    public static Contacts getInstance(Context context){

        if (contacts == null)
            contacts = new Contacts(context);

        return contacts;

    }

    public List<ContactDetail> getContacts() {

        if (contactsDetails != null)
            return contactsDetails;

        contactsDetails = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor;
        try {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                    null, null, null);
        } catch (SecurityException e){
            e.printStackTrace();
            return contactsDetails;
        }

        // Loop for every contact in the phone
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor != null && phoneCursor.moveToNext()) {
                        String number_id = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                        String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactsDetails.add(new ContactDetail(contact_id, name, number, number_id));
                    }

                    if (phoneCursor != null)
                        phoneCursor.close();

                }
            }

            if (cursor != null)
                cursor.close();

            return contactsDetails;

        }

        return contactsDetails;

    }

    /**
     * OJO: El identificador del numero no es el mismo que el del contacto
     * @param id identificador del numero
     * @return el numero de telefono solicitado
     */
    public String getNumberByID(String id){

        if (id == null || id.length() == 0)
            return null;

        String number = null;
        Cursor c;
        try {
            c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone._ID + "=?", new String[]{id}, null);
        } catch (SecurityException e){
            e.printStackTrace();
            return null;
        }
        if (c != null && c.getCount() > 0 && c.moveToFirst())
            number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        if (c != null)
            c.close();

        return number;
    }

    /**
     * OJO: El identificador del numero no es el mismo que el del contacto
     * @param id identificador del numero al que se le desea buscar el nombre
     * @return retorna el nombre del contacto
     */
    public String getNameByID(String id){

        String name = null;
        Cursor c;
        try {
            c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.Contacts._ID + "=?", new String[]{id}, null);
        } catch (SecurityException e){
            e.printStackTrace();
            return null;
        }
        if (c != null && c.getCount() > 0 && c.moveToFirst())
            name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

        if (c != null)
            c.close();

        return name;

    }

    /**
     * Obtener el nombre del contacto teniendo el numero de telefono
     * @param number numero de telefono
     * @return nombre del contacto
     */
    public String getNameByNumber(String number){

        if (number == null || number.length() == 0)
            return null;

        String name = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(
                    uri,
                    new String[]{ContactsContract.CommonDataKinds.Nickname.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                    ContactsContract.PhoneLookup.NUMBER + "=?",
                    new String[]{number},
                    null);
        }
        catch (SecurityException e){
            e.printStackTrace();
            return null;
        }

        if(cursor != null && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
            cursor.close();
        }

        return name;

    }

    public String getIdByName(String name){

        if (name == null || name.length() == 0)
            return null;

        String id = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(name));

        Cursor cursor;
        try {

            cursor = context.getContentResolver().query(
                    uri,
                    new String[]{ContactsContract.Contacts._ID},
                    ContactsContract.PhoneLookup.DISPLAY_NAME + "=?",
                    new String[]{name},
                    null);
        }
        catch (SecurityException e){
            e.printStackTrace();
            return null;
        }

        if(cursor!=null && cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            cursor.close();
        }

        return id;

    }

    /**
     * Obtener el identificador del contacto en general
     * @param number numero para obtener el id del contacto al que corresponde
     * @return identificador del contacto
     */
    @Nullable
    public String getIdByNumber(String number){

        if (number == null || number.length() == 0)
            return null;

        String contactId = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(
                    uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                    ContactsContract.PhoneLookup.NUMBER+"=?",
                    new String[]{number},
                    null);
        }
        catch (SecurityException e){
            e.printStackTrace();
            return null;
        }

        if(cursor!=null && cursor.moveToFirst()) {
            contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            cursor.close();
        }

        return contactId;

    }

    /**
     * Obtener el flujo de entrada para crear la miniatura de la imagen del contacto dado el id del contacto
     * @param contactId identificador del contacto
     * @return flujo de entrada
     */
    private InputStream openThumbnailPhoto(long contactId) {

        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(photoUri,
                    new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        }
        catch (SecurityException e){
            e.printStackTrace();
            return null;
        }

        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    /**
     * Obtener un bitmap de la miniatura de la imagen del contacto
     * @param contactID identificador del contacto
     * @return bitmap construido con la imagen
     */
    public Bitmap getThumbnailPhoto(long contactID){

        InputStream inputStream = openThumbnailPhoto(contactID);
        return inputStream != null ? BitmapFactory.decodeStream(inputStream) : null;

    }

    /**
     * Obtener el flujo de entrada para crear la imagen del contacto dado el id del contacto
     * @param contactId identificador del contacto
     * @return flujo de entrada
     */
    public InputStream openDisplayPhoto(long contactId) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
            try {
                AssetFileDescriptor fd =
                        context.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
                return fd == null ? null : fd.createInputStream();
            } catch (IOException e) {
                return null;
            }
        }
        else {
            return openThumbnailPhoto(contactId);
        }
    }

    /**
     * TODO: Esto es necesario probarlo en versiones menores de Android 4.0
     */
    public Uri getUriDisplayPhoto(String number){
        try {
            long id = Long.parseLong(getIdByNumber(number));
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

            return Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        }
        catch (NumberFormatException e){
            return null;
        }
    }

    /**
     * Obtener un bitmap de la imagen del contacto
     * @param contactID identificador del contacto
     * @return bitmap construido con la imagen
     */
    public Bitmap getDisplayPhoto(long contactID){

        InputStream inputStream = openDisplayPhoto(contactID);
        return BitmapFactory.decodeStream(inputStream);

    }
}
