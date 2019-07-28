package com.jaco.contact.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;

/**
 * Created by osvel on 6/28/16.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    protected static boolean outgoing = false;
    private View alert;

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            outgoing = true;
        }

        if (MPhone.ACTION_PHONE_STATE.equals(intent.getAction())) {
            String state = intent.getStringExtra("state");
            String incomingNumber = intent.getStringExtra("incoming_number");

            MPhone phone = new MPhone(context, outgoing);
            if (alert == null) {
                phone.handlePhoneState(state, incomingNumber);
                alert = phone.getAlert();
            } else {
                phone.setAlert(alert);
                phone.handlePhoneState(state, incomingNumber);
                alert = null;
            }

            if (state != null) {
                new MPhone(context, outgoing).handlePhoneState(state, incomingNumber);
            }
        }

    }

}