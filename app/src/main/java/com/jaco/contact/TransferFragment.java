package com.jaco.contact;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URI;

/**
 * Created by osvel on 7/24/16.
 */
public class TransferFragment extends Fragment implements View.OnClickListener {

    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance() {
        TransferFragment fragment = new TransferFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_transfer, container, false);

        LinearLayout transfer_button = (LinearLayout) rootView.findViewById(R.id.button_transfer);
        transfer_button.setOnClickListener(this);

        LinearLayout change_pin_button = (LinearLayout) rootView.findViewById(R.id.button_change_pin);
        change_pin_button.setOnClickListener(this);

        EditText transfer = (EditText) rootView.findViewById(R.id.transfer);
        transfer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                EditText name_or_number = (EditText) getActivity().findViewById(R.id.name_or_number);
                EditText pin_code = (EditText) rootView.findViewById(R.id.pin_code);
                EditText transfer = (EditText) rootView.findViewById(R.id.transfer);

                String pin = pin_code.getText().toString();
                String number = name_or_number.getText().toString();
                String monto = transfer.getText().toString();

                transfer(number, pin, monto);

                return true;
            }
        });

        return rootView;
    }

    public void buildChangePinCode(){

        AlertDialog.Builder change_pin = new AlertDialog.Builder(getActivity());
        final View change_pin_rootview = getActivity().getLayoutInflater().inflate(R.layout.change_pin_dialog, null);
        change_pin.setView(change_pin_rootview);
        change_pin.setTitle(R.string.change_pin_code);

        change_pin.setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText editText = (EditText) change_pin_rootview.findViewById(R.id.actual_pin_code);
                String actual_pin_code = editText.getText().toString();
                editText = (EditText) change_pin_rootview.findViewById(R.id.new_pin_code);
                String new_pin_code = editText.getText().toString();
                editText = (EditText) change_pin_rootview.findViewById(R.id.confirm_new_pin_code);
                String confirm_new_pin_code = editText.getText().toString();

                if (confirm_new_pin_code.length() == 4 && new_pin_code.length() == 4 && actual_pin_code.length() == 4
                        && confirm_new_pin_code.equals(new_pin_code)) {
                    //cambiar pin
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + Uri.encode("*234*2*" + actual_pin_code + "*" + new_pin_code + "#")));
                    try {
                        getContext().startActivity(intent);
                    }
                    catch (SecurityException e){
                        e.printStackTrace();
                    }
                } else if (actual_pin_code.length() != 4) {
                    //codigo actual incorrecto
                    Toast.makeText(getActivity(), R.string.invalid_pin, Toast.LENGTH_SHORT).show();
                } else if (confirm_new_pin_code.equals(new_pin_code) && new_pin_code.length() != 4) {
                    //invalid new pin code
                    Toast.makeText(getActivity(), R.string.invalid_new_pin, Toast.LENGTH_SHORT).show();
                } else if (!confirm_new_pin_code.equals(new_pin_code)) {
                    //pincode mismatch
                    Toast.makeText(getActivity(), R.string.pin_mismatch, Toast.LENGTH_SHORT).show();
                }
            }
        });

        change_pin.setNegativeButton(R.string.cancel, null);
        change_pin.show();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.button_transfer: {
                EditText name_or_number = (EditText) getActivity().findViewById(R.id.name_or_number);
                EditText pin_code = (EditText) getActivity().findViewById(R.id.pin_code);
                EditText transfer = (EditText) getActivity().findViewById(R.id.transfer);

                String pin = pin_code.getText().toString();
                String number = name_or_number.getText().toString();
                String monto = transfer.getText().toString();

                transfer(number, pin, monto);

                break;
            }
            case R.id.button_change_pin: {
                buildChangePinCode();
                break;
            }
        }
    }

    private void transfer(String number, String pin, String monto){
        String transfer_code = "*234*1*";

        if (pin.length() == 4 && PhoneNumber.isValidNumber(number) && monto.length() == 0){
            //transferencia con centavos
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + Uri.encode(transfer_code + number + "*" + pin + "#")));
            try {
                getContext().startActivity(intent);
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
        }
        else if (pin.length() == 4 && PhoneNumber.isValidNumber(number)) {
            //transferencia sin centavos
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + Uri.encode(transfer_code + number + "*" + pin + "*" + monto + "#")));
            try {
                getContext().startActivity(intent);
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
        }
        else if (!PhoneNumber.isValidNumber(number)){
            //invalid number
            Toast.makeText(getActivity(), R.string.invalid_number, Toast.LENGTH_SHORT).show();
        }
        else if (pin.length() != 4){
            //invalid pin
            Toast.makeText(getActivity(), R.string.invalid_pin, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //mostrar campo de busqueda
        LinearLayout search_layout = (LinearLayout) getActivity().findViewById(R.id.search_layout);

        //mostrar el layout si esta oculto
        if (search_layout != null && search_layout.getVisibility() != View.VISIBLE) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_up);
            animation.setStartOffset(200);
            search_layout.startAnimation(animation);
            search_layout.setVisibility(View.VISIBLE);
        }
    }

    //    public void onButtonPressed(int id) {
//        if (mListener != null) {
//            mListener.onStartFragmentInteraction(id);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnStartFragmentInteractionListener) {
//            mListener = (OnStartFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnStartFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    public interface OnTransferFragmentInteractionListener {
//        void onTransferFragmentInteraction(int id);
//    }

}
