package com.jaco.contact.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.jaco.contact.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class CallListenerService extends AlwaysRunIntentService {

    private static final int NOTIFICATION_ID = 2342;
    private int lastStatus = TelephonyManager.CALL_STATE_IDLE;
    private TelephonyManager telephony;
    private Timer timer;
    private TimerTask timerTask;
    private View alert;

    public CallListenerService() {
        super("CallListenerService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, Utils.getNotification(this));
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, final int startId) {

        final Context context = this;
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        telephony.listen(new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                boolean outgoing = lastStatus == TelephonyManager.CALL_STATE_IDLE && state == TelephonyManager.CALL_STATE_OFFHOOK;

                MPhone phone = new MPhone(context, outgoing);
                if (alert == null) {
                    phone.handlePhoneState(state, incomingNumber);
                    alert = phone.getAlert();
                } else {
                    phone.setAlert(alert);
                    phone.handlePhoneState(state, incomingNumber);
                    alert = null;
                }

                System.out.println("incomingNumber : "+incomingNumber);

                lastStatus = state;
            }

        }, PhoneStateListener.LISTEN_CALL_STATE);

        startTimer();
        return IntentService.START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {



    }

    private void startTimer(){

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("Timer", "Call Service Timer");
            }
        };
        timer.schedule(timerTask, 60000, 5*60*1000);

    }

    private void stopTimer() {

        timer.cancel();
        timer = null;

    }
}
