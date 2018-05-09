package com.jaco.contact;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

/**
 * Created by osvel on 5/7/18.
 */

public class FragmentPlanAmigo extends Fragment implements View.OnClickListener {

    public FragmentPlanAmigo() {}

    public static FragmentPlanAmigo newInstance(){
        return new FragmentPlanAmigo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_friend, container, false);

        View view = rootview.findViewById(R.id.add_number);
        view.setOnClickListener(this);

        view = rootview.findViewById(R.id.remove_number);
        view.setOnClickListener(this);

        view = rootview.findViewById(R.id.activate_plan);
        view.setOnClickListener(this);

        view = rootview.findViewById(R.id.deactivate_plan);
        view.setOnClickListener(this);

        view = rootview.findViewById(R.id.status);
        view.setOnClickListener(this);

        view = rootview.findViewById(R.id.change_count);
        view.setOnClickListener(this);

        return rootview;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.add_number: {
                startChangeNumberFragment(R.id.add_number);
                break;
            }
            case R.id.remove_number: {
                startChangeNumberFragment(R.id.remove_number);
                break;
            }
            case R.id.activate_plan: {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode("*133*4*1*1#")));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.deactivate_plan: {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode("*133*4*1*2#")));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.status: {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode("*133*4*3*1*1#")));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.change_count: {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode("*133*4*3*1*2#")));
                try {
                    getContext().startActivity(intent);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
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

    private void startChangeNumberFragment(int transition){

        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                .replace(R.id.container, FragmentChangeNumber.newInstance(transition))
                .addToBackStack(null)
                .commit();

    }

}
