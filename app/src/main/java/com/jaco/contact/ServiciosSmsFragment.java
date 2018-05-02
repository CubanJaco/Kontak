package com.jaco.contact;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class ServiciosSmsFragment extends Fragment implements View.OnClickListener {

    private OnServiciosSmsInteractionListener mListener;

    public ServiciosSmsFragment() {
        // Required empty public constructor
    }

    public static ServiciosSmsFragment newInstance() {
        ServiciosSmsFragment fragment = new ServiciosSmsFragment();
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_servicios_sms, container, false);

        View entumovil = rootView.findViewById(R.id.button_sms_entumovil);
        entumovil.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        onButtonPressed(view.getId());
    }

    public void onButtonPressed(int id) {
        if (mListener != null) {
            mListener.onServiciosSmsInteraction(id);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnServiciosSmsInteractionListener) {
            mListener = (OnServiciosSmsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnServiciosSmsInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public interface OnServiciosSmsInteractionListener {
        void onServiciosSmsInteraction(int id);
    }

}
