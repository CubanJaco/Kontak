package com.jaco.contact;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ITEM_CLICK_ID = "item_click_id";

    private boolean close;
    private long duration = 150;
    private long offset = 75;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //para coregir bug del menu en android oreo o superior
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        openMenu();
        close = true;

        LinearLayout dismiss_layout = (LinearLayout) findViewById(R.id.dismiss_layout);
        dismiss_layout.setOnClickListener(this);
    }

    public void onClickAnimation(final View view){

        Animation scale_in = new ScaleAnimation(1, 0.75f, 1, 0.75f, 50, 50);
        scale_in.setDuration(duration);
        final Animation scale_out = new ScaleAnimation(0.75f, 1, 0.75f, 1, 50, 50);
        scale_out.setDuration(duration);
        scale_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                closeMenu();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        scale_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scale_out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(scale_in);

    }

    public void openMenu(){

        LinearLayout menu_layout = (LinearLayout) findViewById(R.id.menu_layout);
        int childCount = menu_layout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            Animation animation = new TranslateAnimation(100, 0, 0, 0);
            animation.setDuration(duration);
            animation.setStartOffset(offset*i);
            View child = menu_layout.getChildAt(i);
            child.startAnimation(animation);
            child.setVisibility(View.VISIBLE);
            child.setOnClickListener(this);
        }
    }

    public void closeMenu(){

        if (!close)
            return;

        close = false;
        LinearLayout menu_layout = (LinearLayout) findViewById(R.id.menu_layout);
        int childCount = menu_layout.getChildCount();

        int count = 0;
        for (int i = childCount-1; i >= 0 ; i--) {
            Animation animation = new TranslateAnimation(0, 100, 0, 0);
            animation.setDuration(duration);
            animation.setStartOffset(offset*count++);

            if (i == 0){
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        MenuActivity.this.finish();
                        close = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }

            View child = menu_layout.getChildAt(i);
            child.startAnimation(animation);
            child.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.dismiss_layout: {
                closeMenu();
                setResult(Activity.RESULT_CANCELED, getIntent());
                break;
            }
            default: {
                onClickAnimation(view);
                Intent intent = MenuActivity.this.getIntent();
                intent.putExtra(ITEM_CLICK_ID, view.getId());
                setResult(Activity.RESULT_OK, intent);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        closeMenu();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode){
            case KeyEvent.KEYCODE_MENU: {
                closeMenu();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
