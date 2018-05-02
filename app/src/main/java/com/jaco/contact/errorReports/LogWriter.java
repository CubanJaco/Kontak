package com.jaco.contact.errorReports;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.jaco.contact.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by osvel on 11/16/17.
 */

public class LogWriter {

    private static final String LOG_NAME = "ERROR_LOG";
    private static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy k:m:s", Locale.US);

    public static void writeLog(final Context context, final Object message) {

        try {
            File file = getLogFile(context);

            FileOutputStream fos = new FileOutputStream(file, true);

            if (message != null) {
                fos.write((df.format(new GregorianCalendar().getTime())
                        +"\n"+message)
                        .getBytes());
            }

            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getLogFile(Context context){
        return new File(context.getCacheDir(), LOG_NAME);
    }

    public static void sendErrorReport(Context context){

        String subject = "Reporte de error: Kontak ";
        String body = "";

        String versionName;
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "2.0+";
        }

        int versionCode;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionCode = -1;
        }

        subject += versionName;

        body += "Brand: "+Build.BRAND+"\n";
        body += "Device: "+Build.DEVICE+"\n";
        body += "Model: "+Build.MODEL+"\n";
        body += "Android Version: "+Build.VERSION.RELEASE+"\n";
        body += "Android SDK: "+Build.VERSION.SDK_INT+"\n";
        body += "Kontak Ver:"+versionName+" ("+versionCode+")\n\n";


        body += "-----------------------\n";
        body += "Reporte de error\n\n";

        body += readLogFile(context);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:");
        intent.setData(data);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getResources().getString(R.string.contact_email_clean)});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.send)));

    }

    public static boolean hasErrorLog(Context context){
        return LogWriter.getLogFile(context).exists();
    }

    public static String readLogFile(Context context){

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(getLogFile(context)));
            String line;

            while ((line = br.readLine()) != null){
                text.append(line);
                text.append("\n");
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return text.toString();

    }

}
