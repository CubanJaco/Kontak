package com.jaco.contact;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckImei extends Fragment implements View.OnClickListener {
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

//    private String mParam1;
//    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    private static final String CHECK_IMEI_URL = "https://mi.cubacel.net:8443/AirConnector/rest/AirConnect/getIMEI?imei=";

    public static final int VALID_IMEI = 0;
    public static final int VALID_IMEI_NO_NETWORK = 1;
    public static final int INVALID_IMEI = 2;
    public static final int MALFORMED_IMEI = 3;
    public static final int SHORT_IMEI = 4;

    private EditText imeiLayout;

    public CheckImei() {
        // Required empty public constructor
    }

    public static CheckImei newInstance() {
        return new CheckImei();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_check_imei, container, false);

        LinearLayout checkImeiButton = (LinearLayout) rootView.findViewById(R.id.button_check_imei);
        checkImeiButton.setOnClickListener(this);

        imeiLayout = (EditText) rootView.findViewById(R.id.imei_edit);
        String imei = getUniqueIMEIId(getContext());
        if (imei != null)
            imeiLayout.setText(imei);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //ocultar campo de busqueda
        final LinearLayout search_layout = (LinearLayout) getActivity().findViewById(R.id.search_layout);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.exit_to_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                search_layout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if (search_layout.getVisibility() != View.GONE)
            search_layout.startAnimation(animation);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static String getUniqueIMEIId(Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            return null;

        String imei = telephonyManager.getDeviceId();
        if (imei != null && !imei.isEmpty()) {
            return imei;
        } else {
//            return android.os.Build.SERIAL;
            return null;
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_check_imei) {
            new CheckImei.ChekImei().execute(imeiLayout.getText().toString());
        }

    }

    private static void showImeiDialog(Context context, int message, int icon){

        new AlertDialog.Builder(context)
                .setTitle(R.string.imei_checker)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setIcon(icon)
                .show();

    }

    private Integer checkImeiNetwork(String imei){

        Integer response = null;
        try {
            response = validateImeiNetwork(imei);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (response == null)
            return null;

        switch (response) {
            case 0: {
                return VALID_IMEI;
            }
            case 2: {
                return SHORT_IMEI;
            }
            case 3: {
                return MALFORMED_IMEI;
            }
            case 4: {
                return INVALID_IMEI;
            }
            default: {
                return null;
            }
        }

    }

    private static Integer validateImeiNetwork(String imei) throws IOException {

        URL url = new URL(CHECK_IMEI_URL+imei);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(3000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();

        InputStream is = conn.getInputStream();
        StringBuilder sb = new StringBuilder();
        int r;
        do {
            r = is.read();

            if (r != -1){
                sb.append((char) r);
            }

        } while (r != -1);

        JSONObject object;
        try {
            object = new JSONObject(sb.toString());
        } catch (JSONException e) {
            object = null;
        }

        Integer response = null;
        try {
            response = object != null ? object.getInt("getIMEIResult") : null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }

    private class ChekImei extends AsyncTask<String, Void, Integer> {

        private WeakReference<Context> weak;
        private WeakReference<ProgressDialog> progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weak = new WeakReference<>(getContext());
            ProgressDialog progressDialog = ProgressDialog.show(getContext(), "",
                    getContext().getString(R.string.checking_imei), true);
            progressDialog.show();
            progress = new WeakReference<>(progressDialog);
        }

        @Override
        protected Integer doInBackground(String... params) {
            if (params.length == 0)
                throw new IllegalStateException("Params can't be empty");

            String imei = params[0];

            Integer network = null;

            int validate = Utils.validateImei(imei);
            if (validate == VALID_IMEI_NO_NETWORK){
                network = checkImeiNetwork(imei);
                Log.d("LOL", "doInBackground: "+network);
            }

            return network == null ? validate : network;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (progress.get() != null)
                progress.get().dismiss();

            switch (integer) {
                case VALID_IMEI: {
                    showImeiDialog(weak.get(), R.string.valid_imei, R.drawable.ok);
                    break;
                }
                case VALID_IMEI_NO_NETWORK: {
                    showImeiDialog(weak.get(), R.string.valid_imei_no_network, R.drawable.warning);
                    break;
                }
                case INVALID_IMEI: {
                    showImeiDialog(weak.get(), R.string.invalid_imei, R.drawable.error);
                    break;
                }
                case MALFORMED_IMEI: {
                    showImeiDialog(weak.get(), R.string.malformed_imei, R.drawable.error);
                    break;
                }
                case SHORT_IMEI: {
                    showImeiDialog(weak.get(), R.string.short_imei, R.drawable.error);
                    break;
                }
            }

        }
    }
}
