package com.jaco.contact;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.GregorianCalendar;

public class SplashActivity extends AppCompatActivity {

    public static long openTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        openTime = new GregorianCalendar().getTimeInMillis();

        ImageView loading = (ImageView) findViewById(R.id.loading_splash);

        RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(4000);
        rotate.setInterpolator(new AccelerateDecelerateInterpolator());
        rotate.setRepeatCount(Animation.INFINITE);

        loading.startAnimation(rotate);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 5000);

    }

    @Override
    public void onBackPressed() {}
}
