package com.jaco.contact;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.jaco.contact.preferences.mSharedPreferences;

public class CreditFragment extends Fragment implements View.OnClickListener {

    public CreditFragment() {
        // Required empty public constructor
    }

    public static CreditFragment newInstance() {
        CreditFragment fragment = new CreditFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_credit, container, false);

        View view = rootView.findViewById(R.id.button_saldo);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.button_saldo_bono);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.button_saldo_bolsa);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.button_saldo_sms);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.button_saldo_voz);
        view.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.button_saldo: {
                //consultar saldo
                Intent intent = new Intent(Intent.ACTION_CALL);
                String credit = mSharedPreferences.getCreditConsult(getActivity());
                intent.setData(Uri.parse("tel:" + Uri.encode(credit)));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.button_saldo_bono: {
                //consultar saldo bono
                Intent intent = new Intent(Intent.ACTION_CALL);
                String credit = "*222*266#";
                intent.setData(Uri.parse("tel:" + Uri.encode(credit)));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.button_saldo_bolsa: {
                //consultar saldo
                Intent intent = new Intent(Intent.ACTION_CALL);
                String credit = "*222*328#";
                intent.setData(Uri.parse("tel:" + Uri.encode(credit)));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.button_saldo_sms: {
                //consultar saldo
                Intent intent = new Intent(Intent.ACTION_CALL);
                String credit = "*222*767#";
                intent.setData(Uri.parse("tel:" + Uri.encode(credit)));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.button_saldo_voz: {
                //consultar saldo
                Intent intent = new Intent(Intent.ACTION_CALL);
                String credit = "*222*869#";
                intent.setData(Uri.parse("tel:" + Uri.encode(credit)));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void selectDialog(int titleId, String[] array, final String[] call_number){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this.getContext());
        dialog.setTitle(titleId);

        dialog.setItems(array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //consultar saldo
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode(call_number[i])));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();

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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
