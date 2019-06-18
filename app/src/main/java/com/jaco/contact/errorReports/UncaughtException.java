package com.jaco.contact.errorReports;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * This helper class allows to notify app unhandled exceptions.
 * Created by Fredy Mederos Leon on 04/05/2015
 */
public class UncaughtException implements Thread.UncaughtExceptionHandler {

    private static Context context;
    private Thread.UncaughtExceptionHandler uncaughtException;

    private UncaughtException(Context ctext, Thread.UncaughtExceptionHandler uncaughtException) {
        context = ctext;
        this.uncaughtException = uncaughtException;
    }

    /**
     * This function should be called from your app class. It start the crash reporter.
     *
     * @param context App context.
     */
    public static void startCrashReporter(Context context) {
        Thread.UncaughtExceptionHandler uncaughtException = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtException(context, uncaughtException));
    }

    /**
     * The thread is being terminated by an uncaught exception. Further
     * exceptions thrown in this method are prevent the remainder of the
     * method from executing, but are otherwise ignored.
     *
     * @param thread the thread that has an uncaught exception
     * @param ex     the exception that was thrown
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        try {

            new Thread() {
                @Override
                public void run() {
                    Writer result = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(result);
                    ex.printStackTrace(printWriter);
                    String errorTrace = result.toString();

                    result = new StringWriter();
                    printWriter = new PrintWriter(result);
                    final String caused = "Caused by:";
                    if (errorTrace.contains(caused)) {
                        errorTrace = errorTrace.split(caused)[0];
                        ex.getCause().printStackTrace(printWriter);
                        errorTrace += caused +" "+ result.toString();
                    }

                    LogWriter.writeLog(context, errorTrace);

                }
            }.start();

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(0);
        }
        uncaughtException.uncaughtException(thread, ex);
    }

}
