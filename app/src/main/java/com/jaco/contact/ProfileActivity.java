package com.jaco.contact;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaco.contact.preferences.mSharedPreferences;
import com.jaco.contact.service.IncomingSmsReceiver;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PHONE_ENTRY = "phone_entry";

    private PhoneEntry phoneEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //prepare toolbar
        AppBarLayout app_bar = (AppBarLayout) findViewById(R.id.app_bar);
        app_bar.setExpanded(true, true);

        //get phoneEntry
        Bundle bundle = getIntent().getExtras();
        phoneEntry = (PhoneEntry) bundle.getSerializable(PHONE_ENTRY);

        Contacts contacts = Contacts.getInstance(this);

        String contactName = contacts.getNameByNumber(phoneEntry.getNumber());
        boolean isContact = contactName != null;
        String name = isContact && contactName.length() != 0 ? contactName : phoneEntry.getName();

        if (name == null || name.length() == 0)
            name = getResources().getString(R.string.unknown);

        //set toolbar name
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) app_bar.findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle(name);

        //create transformation
        final Transformation transformation =
                new RoundedTransformationBuilder()
                    .cornerRadiusDp(90)
                    .oval(false)
                    .build();

        //get imageView
        final ImageView avatar = (ImageView) collapsingToolbarLayout.findViewById(R.id.avatar);

        //get photoUri
        PhoneNumber phoneNumber = new PhoneNumber(phoneEntry.getNumber());
        Uri displayPhotoUri = contacts.getUriDisplayPhoto(phoneNumber.getNumber());

        //insert image on imageView
        final int dim = (int) getResources().getDimension(R.dimen.avatar_size);
        Callback callback = new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(ProfileActivity.this)
                        .load(R.drawable.user)
                        .resize(dim, dim)
                        .transform(MainActivity.transformation)
                        .into(avatar);
            }
        };

        if (displayPhotoUri != null){
            Picasso.with(this)
                .load(displayPhotoUri)
                .resize(dim, dim)
                .transform(transformation)
                .into(avatar, callback);
        }
        else {
            Picasso.with(ProfileActivity.this)
                .load(R.drawable.user)
                .resize(dim, dim)
                    .transform(transformation)
                .into(avatar);
        }

        TextView textView;
        if (phoneEntry.getType() != PhoneType.UNKNOWN){
            textView = (TextView) findViewById(R.id.owner);
            textView.setText(phoneEntry.getName());
            textView.setOnClickListener(this);

            textView = (TextView) findViewById(R.id.address);
            textView.setText(phoneEntry.getAddress());
            textView.setOnClickListener(this);

            textView = (TextView) findViewById(R.id.province);
            textView.setText(phoneEntry.getProvinceName(this));
            textView.setOnClickListener(this);
        }
        else {
            textView = (TextView) findViewById(R.id.owner);
            textView.setText(getResources().getString(R.string.unknown));
            textView.setOnClickListener(this);

            findViewById(R.id.CI_layout).setVisibility(View.GONE);
            findViewById(R.id.address_layout).setVisibility(View.GONE);
            findViewById(R.id.province_layout).setVisibility(View.GONE);
            findViewById(R.id.birth_date_layout).setVisibility(View.GONE);
            findViewById(R.id.age_layout).setVisibility(View.GONE);
        }

        if (!phoneNumber.isMovil()){
            findViewById(R.id.layout_send_sms).setVisibility(View.GONE);
            findViewById(R.id.layout_free_call).setVisibility(View.GONE);
            findViewById(R.id.layout_button_transfer).setVisibility(View.GONE);
        }

        textView = (TextView) findViewById(R.id.number);
        textView.setText(phoneEntry.getNumber());
        textView.setOnClickListener(this);

        //si es un fijo ocultar edad y fecha de nacimiento, de lo contrario calcular
        if (phoneEntry.getType() == PhoneType.FIX){
            findViewById(R.id.birth_date_layout).setVisibility(View.GONE);
            findViewById(R.id.age_layout).setVisibility(View.GONE);
            findViewById(R.id.CI_layout).setVisibility(View.GONE);
        }
        else if (!phoneEntry.isValidIdentification()){
            textView = (TextView) findViewById(R.id.CI);
            textView.setText(phoneEntry.getIdentification());
            textView.setOnClickListener(this);

            findViewById(R.id.birth_date_layout).setVisibility(View.GONE);
            findViewById(R.id.age_layout).setVisibility(View.GONE);
        }
        else {
            textView = (TextView) findViewById(R.id.CI);
            textView.setText(phoneEntry.getIdentification());
            textView.setOnClickListener(this);

            textView = (TextView) findViewById(R.id.birth_date);
            textView.setText(phoneEntry.getBirthDateString());
            textView.setOnClickListener(this);

            textView = (TextView) findViewById(R.id.age);
            textView.setText(String.format(Locale.US, "%d", phoneEntry.getAge()));
            textView.setOnClickListener(this);
        }

        if (isContact)
            findViewById(R.id.layout_add_contact).setVisibility(View.GONE);

        LinearLayout button = (LinearLayout) findViewById(R.id.button_add_contact);
        button.setOnClickListener(this);
        button = (LinearLayout) findViewById(R.id.button_call);
        button.setOnClickListener(this);
        button = (LinearLayout) findViewById(R.id.button_free_call);
        button.setOnClickListener(this);
        button = (LinearLayout) findViewById(R.id.button_send_sms);
        button.setOnClickListener(this);
        button = (LinearLayout) findViewById(R.id.button_transfer);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        onClickAnimation(view);

        switch (id){
            case R.id.button_add_contact: {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType("vnd.android.cursor.dir/contact");
                intent.putExtra("name", phoneEntry.getName());
                intent.putExtra("phone", phoneEntry.getNumber());
//                intent.putExtra("postal", phoneEntry.getAddress());
                startActivity(intent);
                break;
            }
            case R.id.button_call: {

                PhoneNumber phoneNumber = new PhoneNumber(phoneEntry.getNumber());
                String number = phoneNumber.getNumber();

                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode(number)));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.button_free_call: {
                PhoneNumber phoneNumber = new PhoneNumber(phoneEntry.getNumber());
                String number = phoneNumber.getNumber();

                //actualizar el prefijo segun las preferencias
                String prefix = mSharedPreferences.getCallPrefix(this);

                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + Uri.encode(prefix+number)));
                try {
                    startActivity(intent);
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }
                break;
            }
            case R.id.button_send_sms: {
                PhoneNumber phoneNumber = new PhoneNumber(phoneEntry.getNumber());
                String number = phoneNumber.getNumber();

                long sms_id = IncomingSmsReceiver.findThreadIdFromAddress(this, number);

                Intent sms_thread = new Intent(Intent.ACTION_VIEW);
                sms_thread.setData(Uri.parse("content://mms-sms/conversations/" + sms_id));

                startActivity(sms_thread);
                break;
            }
            case R.id.button_transfer: {
//                Intent intent = new Intent(this, ProfileActivity.class);
                Intent intent = new Intent(this, WidgetContainerActivity.class);
                intent.putExtra(WidgetContainerActivity.FRAGMENT_INDEX, WidgetContainerActivity.TRANSFER);
                intent.putExtra(WidgetContainerActivity.PHONE_NUMBER, phoneEntry.getNumber());
                intent.setAction(Utils.ACTION_TRANSFER);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                } else {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                }

                intent.putExtra(ProfileActivity.PHONE_ENTRY, phoneEntry);

                startActivity(intent);
                break;
            }
            case R.id.owner: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
            case R.id.CI: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
            case R.id.number: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
            case R.id.address: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
            case R.id.province: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
            case R.id.birth_date: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
            case R.id.age: {
                TextView textView = (TextView) view;
                Utils.copyToClipboard(this, textView.getText().toString());
                break;
            }
        }
    }

    public static void onClickAnimation(final View view){

        int duration = 150;

        Animation scale_in = new ScaleAnimation(1, 0.75f, 1, 0.75f, 50, 50);
        scale_in.setDuration(duration);
        final Animation scale_out = new ScaleAnimation(0.75f, 1, 0.75f, 1, 50, 50);
        scale_out.setDuration(duration);
        scale_in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.startAnimation(scale_out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(scale_in);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()){
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
