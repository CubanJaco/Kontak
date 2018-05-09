package com.jaco.contact;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by osvel on 5/7/18.
 */

public class FragmentChangeNumber extends Fragment implements View.OnClickListener {

    private static final String TRANSITION_ID = "transition_id";

    private int transitionId;

    public FragmentChangeNumber() {}

    public static FragmentChangeNumber newInstance(int transitionId) {
        FragmentChangeNumber fragment = new FragmentChangeNumber();
        Bundle args = new Bundle();
        args.putInt(TRANSITION_ID, transitionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            transitionId = getArguments().getInt(TRANSITION_ID);
        else
            transitionId = 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_change_number, container, false);

        //terminar la animacion compartida
        View transitionView = rootView.findViewById(transitionId);
        transitionView.setVisibility(View.VISIBLE);
        transitionView.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

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
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
    }

    @Override
    public void onClick(View v) {

        EditText name_or_number = (EditText) getActivity().findViewById(R.id.name_or_number);
        String number = name_or_number.getText().toString();

        if (!PhoneNumber.isValidNumber(number)) {
            Toast.makeText(getActivity(), R.string.invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }

        String cal = "*133*4*2*1*"+number+"#";
        if (transitionId == R.id.remove_number)
            cal = "*133*4*2*2*"+number+"#";

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + Uri.encode(cal)));
        try {
            getContext().startActivity(intent);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }

        name_or_number.setText("");

    }
}
