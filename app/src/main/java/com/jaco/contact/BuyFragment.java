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

public class BuyFragment extends Fragment implements View.OnClickListener {

    public BuyFragment() {
        // Required empty public constructor
    }

    public static BuyFragment newInstance() {
        BuyFragment fragment = new BuyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_buy_plan, container, false);

        View view = rootView.findViewById(R.id.buy_nauta);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.buy_voice);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.buy_sms);
        view.setOnClickListener(this);

        view = rootView.findViewById(R.id.buy_friend);
        view.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.buy_nauta: {
                Intent intent = new Intent(Intent.ACTION_CALL);
                String credit = "*133*1*2#";
                intent.setData(Uri.parse("tel:" + Uri.encode(credit)));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.buy_sms: {
                String[] array = getResources().getStringArray(R.array.buy_sms);
                String[] call_number = getResources().getStringArray(R.array.buy_sms_numbers);
                int title = R.string.buy_sms_package;
                selectDialog(title, array, call_number);
                break;
            }
            case R.id.buy_voice: {
                String[] array = getResources().getStringArray(R.array.buy_voice);
                String[] call_number = getResources().getStringArray(R.array.buy_voice_numbers);
                int title = R.string.buy_voice_package;
                selectDialog(title, array, call_number);
                break;
            }
            case R.id.buy_friend: {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(R.id.container, FragmentPlanAmigo.newInstance())
                        .addToBackStack(null)
                        .commit();
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
