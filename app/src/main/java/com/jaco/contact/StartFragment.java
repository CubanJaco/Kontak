package com.jaco.contact;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.jaco.contact.preferences.mSharedPreferences;

public class StartFragment extends Fragment implements View.OnClickListener {

    private static final int PICK_CONTACT_FREE = 321;
    private static final int PICK_CONTACT_PRIVATE = 231;

    private OnStartFragmentInteractionListener mListener;

    public StartFragment() {
        // Required empty public constructor
    }

    public static StartFragment newInstance() {
        StartFragment fragment = new StartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_start, container, false);

        LinearLayout gratis_button = (LinearLayout) rootView.findViewById(R.id.button_free);
        gratis_button.setOnClickListener(this);

        LinearLayout private_button = (LinearLayout) rootView.findViewById(R.id.button_private);
        private_button.setOnClickListener(this);

        LinearLayout credit_button = (LinearLayout) rootView.findViewById(R.id.button_saldo);
        credit_button.setOnClickListener(this);

        LinearLayout buy_all = (LinearLayout) rootView.findViewById(R.id.button_buy_all);
        buy_all.setOnClickListener(this);

        LinearLayout recarga_button = (LinearLayout) rootView.findViewById(R.id.button_recarga);
        recarga_button.setOnClickListener(this);

        LinearLayout transfer_button = (LinearLayout) rootView.findViewById(R.id.button_transfer);
        transfer_button.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.button_transfer: {
                mListener.onStartFragmentInteraction(id);
                break;
            }
            case R.id.button_recarga: {
                buildRecargaMessage();
                break;
            }
            case R.id.button_free: {
                //abrir agenda de contactos

                EditText receiver = (EditText) getActivity().findViewById(R.id.name_or_number);
                String number = receiver.getText().toString();
                if (PhoneNumber.isValidNumber(number)) {
                    freeCall(number);
                } else {
                    Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(it, PICK_CONTACT_FREE);
                }
                break;
            }
            case R.id.button_private: {
                //abrir agenda de contactos
                EditText receiver = (EditText) getActivity().findViewById(R.id.name_or_number);
                String number = receiver.getText().toString();
                if (PhoneNumber.isValidNumber(number)) {
                    privateCall(number);
                } else {
                    Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                    startActivityForResult(it, PICK_CONTACT_PRIVATE);
                }
                break;
            }
            case R.id.button_saldo: {
                mListener.onStartFragmentInteraction(id);
                break;
            }
            case R.id.button_buy_all: {
                mListener.onStartFragmentInteraction(id);
                break;
            }
        }
    }

    public void buildRecargaMessage() {

        AlertDialog.Builder recarga_dialog = new AlertDialog.Builder(getActivity());
        final View filterRootView = getActivity().getLayoutInflater().inflate(R.layout.recargar_dialog, null);
        recarga_dialog.setView(filterRootView);
        recarga_dialog.setTitle(R.string.reload_title);

        recarga_dialog.setPositiveButton(R.string.increase, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = (EditText) filterRootView.findViewById(R.id.reload_code);
                String code = editText.getText().toString();
                if (code.length() == 16) {
                    //recargar saldo
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + Uri.encode("*662*" + code + "#")));
                    try {
                        getContext().startActivity(intent);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.invalid_code, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recarga_dialog.setNegativeButton(R.string.cancel, null);
        recarga_dialog.show();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //mostrar campo de busqueda
        LinearLayout search_layout = (LinearLayout) getActivity().findViewById(R.id.search_layout);

        //mostrar el layout si esta oculto
        if (search_layout.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_up);
            animation.setStartOffset(200);
            search_layout.startAnimation(animation);
            search_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnStartFragmentInteractionListener) {
            mListener = (OnStartFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStartFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnStartFragmentInteractionListener {
        void onStartFragmentInteraction(int id);
    }

    public void freeCall(String number) {

        //obtener un numero valido para la llamada con 99
        PhoneNumber phoneNumber = new PhoneNumber(number);
        number = phoneNumber.getNumber();

        //actualizar el prefijo segun las preferencias
        String prefix = mSharedPreferences.getCallPrefix(getActivity());

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + Uri.encode(prefix + number)));
        try {
            getContext().startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    public void privateCall(String number) {

        //obtener un numero valido para la llamada con 99
        PhoneNumber phoneNumber = new PhoneNumber(number);
        number = phoneNumber.getNumber();

        String prefix = "#31#";

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + Uri.encode(prefix + number)));
        try {
        getContext().startActivity(intent);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {

                case PICK_CONTACT_FREE: {

                    Contacts contacts = Contacts.getInstance(getContext());
                    String id = data.getData().getLastPathSegment();
                    String number = contacts.getNumberByID(id);
                    PhoneNumber phoneNumber = new PhoneNumber(number);
                    number = phoneNumber.getNumber();

                    freeCall(number);

                    break;
                }
                case PICK_CONTACT_PRIVATE: {

                    Contacts contacts = Contacts.getInstance(getContext());
                    String id = data.getData().getLastPathSegment();
                    String number = contacts.getNumberByID(id);
                    PhoneNumber phoneNumber = new PhoneNumber(number);
                    number = phoneNumber.getNumber();

                    privateCall(number);

                    break;
                }

            }

        }

    }
}
