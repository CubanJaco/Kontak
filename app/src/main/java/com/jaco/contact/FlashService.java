package com.jaco.contact;

import android.app.IntentService;
import android.content.Intent;

import com.jaco.contact.preferences.mSharedPreferences;

/**
 * Created by osvel on 8/23/16.
 */
public class FlashService extends IntentService {

    public FlashService() {
        super(FlashService.class.toString());
    }

    public FlashService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        FlashController flashController = FlashController.getInstance();
        flashController.initCamera();

        int on_time = mSharedPreferences.getFlashOnTime(getApplicationContext());
        int off_time = mSharedPreferences.getFlashOffTime(getApplicationContext());

        int wait;
        do {
            flashController.switchFlash();

            if (flashController.isTurnOn())
                wait = on_time;
            else
                wait = off_time;

            synchronized (this) {
                try {
                    wait(wait);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            flashController = FlashController.getInstance();
        } while (flashController.isInit());
        stopSelf();

    }
}
