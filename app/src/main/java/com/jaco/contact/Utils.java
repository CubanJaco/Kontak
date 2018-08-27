package com.jaco.contact;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Region;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.jaco.contact.preferences.mSharedPreferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by osvel on 9/1/16.
 */
public class Utils {

    public static final String ACTION_CALL = "ACTION_CALL";
    public static final String ACTION_FREE_CALL = "ACTION_FREE_CALL";
    public static final String ACTION_UNKNOWN_CALL = "ACTION_UNKNOWN_CALL";
    public static final String ACTION_TRANSFER = "ACTION_TRANSFER";

    public static int validateImei(String imei) {

        //si la longitud del imei es distinta de 15 es invalido
        //si el imei contiene letras es invalido
        if (imei.length() != 15)
            return CheckImei.SHORT_IMEI;

        if (!PhoneNumber.allNumbers(imei))
            return CheckImei.MALFORMED_IMEI;

        //obtener el ultimo digito como numero
        int last = imei.charAt(14) - 48;

        //duplicar cada segundo digito
        //sumar cada uno de los digitos resultantes del nuevo imei
        int curr;
        int sum = 0;
        for (int i = 0; i < 14; i++) {
            curr = imei.charAt(i) - 48;
            if (i % 2 != 0)
                sum += duplicateAndSum(curr);
            else
                sum += curr;
        }

        //redondear al multiplo de 10 superior mas cercano
        int round = sum % 10 == 0 ? sum : ((sum / 10 + 1) * 10);

        return (round - sum == last) ? CheckImei.VALID_IMEI_NO_NETWORK : CheckImei.INVALID_IMEI;

    }

    private static int duplicateAndSum(int n) {

        switch (n) {
            case 5: {
                //5+5 = 10
                //1+0 = 1
                return 1;
            }
            case 6: {
                //6+6 = 12
                //1+2 = 3
                return 3;
            }
            case 7: {
                //7=7 = 14
                //1+4 = 5
                return 5;
            }
            case 8: {
                //8+8 = 16
                //1+6 = 7
                return 7;
            }
            case 9: {
                //9+9 = 18
                //1+8 = 9
                return 9;
            }
            default: {
                return n*2;
            }
        }

    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri){

        AssetFileDescriptor fd;
        InputStream is = null;
        try {
            fd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            is = fd.createInputStream();
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
        }

        Bitmap bm = null;
        if (is != null)
            bm = BitmapFactory.decodeStream(is);

        return bm;

    }

    public static Bitmap circleBitmap(Context context, Uri uri){
        return circleBitmap(getBitmapFromUri(context, uri));
    }

    public static Bitmap circleBitmap(Context context, int resource){
        return circleBitmap(BitmapFactory.decodeResource(context.getResources(), resource));
    }

    public static Bitmap circleBitmap(Bitmap circleImage){

        if (circleImage == null)
            return null;

        //create empty bitmap
        Bitmap image = Bitmap.createBitmap(circleImage.getWidth(), circleImage.getHeight(), Bitmap.Config.ARGB_8888);
        image = image.copy(image.getConfig(), true);

        //create canvas with bitmap
        Canvas canvas = new Canvas(image);

        //get size of canvas
        float size_x = canvas.getWidth();
        float size_y = canvas.getHeight();

        //set transparent color
        canvas.drawColor(Color.parseColor("#00ffffff"));

        Path mPath = new Path();
        mPath.setLastPoint(size_x, size_y);
        canvas.clipPath(mPath); // makes the clip empty
        mPath.addCircle(size_x / 2, size_y / 2, Math.min(size_x, size_y) / 2, Path.Direction.CCW);
        canvas.clipPath(mPath, Region.Op.UNION);

        //draw bitmap
        canvas.drawBitmap(circleImage, 0, 0, new Paint());

        return image;
    }

    public static boolean verifyDatabase(Context context, String path){

        if (path == null || !path.endsWith(".db"))
            return false;

        File fileDatabase = new File(path);
        if (!fileDatabase.exists())
            return false;

        EtecsaDB db = new EtecsaDB(context, path);

        String number;
        try {
            number = db.searchMobileById(1);
        }
        catch (Exception e){
            return false;
        }

        if (number == null)
            return false;

        if (number.length() == 10 && number.startsWith("53")){
            mSharedPreferences.setAlternativeDatabase(context, false);
            return true;
        }

        if (number.length() == 8){
            mSharedPreferences.setAlternativeDatabase(context, true);
            return true;
        }

        return false;
    }

    public static List<String> searchDatabase(File file){

        List<String> paths = new ArrayList<>();

        if (!file.exists())
            return paths;

        if (file.isFile() && file.getName().equalsIgnoreCase("etecsa.db")){
            paths.add(file.getAbsolutePath());
            return paths;
        }

        File[] files = file.listFiles();
        if (files == null) {
            return paths;
        }

        for (File current : files){
            if (current.isFile() && current.getName().equalsIgnoreCase("etecsa.db")){
                paths.add(current.getAbsolutePath());
            }
            else if (current.isDirectory())
                paths.addAll(searchDatabase(current));
        }

        return paths;
    }

    public static boolean selectDatabase(Context context, File external){

        List<String> paths = searchDatabase(external);

        if (paths.size() == 1 && verifyDatabase(context, paths.get(0))){
            mSharedPreferences.setDatabasePath(context, paths.get(0));
            return true;
        }

        if (paths.size() <= 1){
            return false;
        }

        String alternativePath = null;
        for (String path : paths) {
            boolean verify = verifyDatabase(context, path);
            if (verify && !mSharedPreferences.isAlternativeDatabase(context)){
                mSharedPreferences.setDatabasePath(context, path);
                return true;
            }
            else if (verify){
                alternativePath = path;
            }
        }

        if (alternativePath == null)
            return false;

        mSharedPreferences.setDatabasePath(context, alternativePath);
        return true;

    }

    public static Bitmap drawerBorderColored(Context context){

        Bitmap maskImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.drawer_border);
        maskImage = maskImage.copy(maskImage.getConfig(), true);

        Bitmap mainImage = Bitmap.createBitmap(maskImage.getWidth(), maskImage.getHeight(), Bitmap.Config.ARGB_8888);

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            color = context.getResources().getColor(R.color.colorPrimary, context.getTheme());
        else
            color = context.getResources().getColor(R.color.colorPrimary);

        mainImage.eraseColor(color);

        Canvas canvas = new Canvas();
        Bitmap result = Bitmap.createBitmap(maskImage.getWidth(), maskImage.getHeight(), Bitmap.Config.ARGB_8888);

        canvas.setBitmap(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        canvas.drawBitmap(mainImage, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(maskImage, 0, 0, paint);
        paint.setXfermode(null);

        return result;

    }


    /**
     * Enviar un sms sin interaccion del usuario
     * @param phoneNo Numero al que se envía el sms
     * @param msg texto del sms a enviar
     */
    public static void sendSMS(String phoneNo, String msg){

        System.out.println("lol message send to: "+phoneNo+" ["+msg+"]");

//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    public static void consultCreditDialog(final Context context){

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.credit);

        String[] array = new String[]{
                context.getResources().getString(R.string.credit_consult),
                context.getResources().getString(R.string.bonus_credit),
                context.getResources().getString(R.string.bag_credit),
                context.getResources().getString(R.string.voice_credit),
                context.getResources().getString(R.string.sms_credit)
        };
        final String[] call_number = new String[]{
                "*222#",
                "*222*266#",
                "*222*328#",
                "*222*869#",
                "*222*767#"
        };

        dialog.setItems(array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //consultar saldo
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode(call_number[i])));
                try {
                    context.startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) view = new View(activity);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
