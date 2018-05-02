package com.jaco.contact;

import android.content.Context;
import android.net.Uri;

/**
 * Created by osvel on 7/28/16.
 */
public class CallDetails {

    private MyCallLog.Call call;
    private String name;
    private Uri image_uri;

    public CallDetails(Context context, MyCallLog.Call call) {
        this.call = call;

        boolean isContact = false;
        Contacts contacts = Contacts.getInstance(context);
        PhoneNumber phoneNumber = new PhoneNumber(call.getNumber());

        //obtener el numero
        final String number = phoneNumber.getNumber();
        //obtener el nombre de contacto
        name = contacts.getNameByNumber(number);

        //si no hay nombre de contacto buscar en la base de datos
        PhoneEntry[] phones = null;
        if (name == null || name.length() == 0) {
            EtecsaDB db = new EtecsaDB(context);
            phones = db.searchByNumber(number);
        }
        else
            isContact = true;

        //si la base de datos dio resultado tomar la primera coincidencia del numero
        if (phones != null && phones.length > 0)
            name = phones[0].getName();
            //si la base de datos no dio resultado poner desconocido
        else if (name == null || name.length() == 0)
            name = context.getResources().getString(R.string.unknown);

        if (isContact)
            image_uri = contacts.getUriDisplayPhoto(number);
        else
            image_uri = null;
    }

    public String getName() {
        return name;
    }

    public Uri getImageUri() {
        return image_uri;
    }

    public MyCallLog.Call getCall() {
        return call;
    }
}
