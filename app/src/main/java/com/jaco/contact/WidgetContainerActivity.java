package com.jaco.contact;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaco.contact.preferences.mSharedPreferences;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class WidgetContainerActivity extends AppCompatActivity implements Callback, View.OnClickListener {

    public static final String FRAGMENT_INDEX = "FRAGMENT_INDEX";

    public static final int FREE_CALL = 1;
    public static final int TRANSFER = 2;
    public static final int UNKNOWN_CALL = 3;

    private static final int PICK_CONTACT_FREE = 111;
    private static final int PICK_CONTACT_UNKNOWN = 333;
    private static final int PICK_CONTACT = 222;

    public static final String PHONE_NUMBER = "phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
        }

        int value = extras.getInt(FRAGMENT_INDEX);

        switch (value) {
            case FREE_CALL: {
                getSupportActionBar().setTitle(R.string.free_call);
                Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(it, PICK_CONTACT_FREE);
                break;
            }
            case UNKNOWN_CALL: {
                getSupportActionBar().setTitle(R.string.free_call);
                Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(it, PICK_CONTACT_UNKNOWN);
                break;
            }
            case TRANSFER: {

                //get phoneEntry
                String number = extras.getString(PHONE_NUMBER);

                final EditText find = (AutoCompleteTextView) findViewById(R.id.name_or_number);
                if (number != null){
                    find.setText(number);
                }

                new MainActivity.GetContacts(this).execute();

                final ImageView contact_image = (ImageView) findViewById(R.id.contact_image);
                contact_image.setOnClickListener(this);

                final int dim = (int) getResources().getDimension(R.dimen.avatar_size);
                Picasso.with(this)
                        .load(R.drawable.user)
                        .resize(dim, dim)
                        .transform(MainActivity.transformation)
                        .into(contact_image);

                final Contacts contacts = Contacts.getInstance(this);

                TextView contact_name = (TextView) this.findViewById(R.id.contact_name);
                if (number == null) {
                    find.addTextChangedListener(new mTextWatcher(
                            contact_name, contact_image, contacts
                    ));
                }
                else {
                    if (PhoneNumber.isValidNumber(number)){
                        String name = contacts.getNameByNumber(number);
                        Uri imageUri = contacts.getUriDisplayPhoto(number);

                        if (imageUri != null){

                            Picasso.with(WidgetContainerActivity.this)
                                    .load(imageUri)
                                    .resize(dim, dim)
                                    .transform(MainActivity.transformation)
                                    .into(contact_image, WidgetContainerActivity.this);

                        }
                        contact_name.setText(name);
                    }
                }

                getSupportActionBar().setTitle(R.string.transfer);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, TransferFragment.newInstance())
                        .commit();
                break;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            switch (requestCode){
                case PICK_CONTACT_FREE: {

                    Contacts contacts = Contacts.getInstance(this);
                    String id = data.getData().getLastPathSegment();
                    String number = contacts.getNumberByID(id);
                    PhoneNumber phoneNumber = new PhoneNumber(number);
                    number = phoneNumber.getNumber();

                    //actualizar el prefijo segun las preferencias
                    String prefix = mSharedPreferences.getCallPrefix(this);

                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + Uri.encode(prefix + number)));
                    try {
                        startActivity(intent);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }

                    new Handler().postDelayed(new Runnable() {
                                                  public void run() {
                                                      WidgetContainerActivity.this.finish();
                                                  }
                                              }, 3000
                    );
                    break;

                }
                case PICK_CONTACT_UNKNOWN: {

                    Contacts contacts = Contacts.getInstance(this);
                    String id = data.getData().getLastPathSegment();
                    String number = contacts.getNumberByID(id);
                    PhoneNumber phoneNumber = new PhoneNumber(number);
                    number = phoneNumber.getNumber();

                    //actualizar el prefijo segun las preferencias
                    String prefix = "#31#";

                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + Uri.encode(prefix + number)));
                    try {
                        startActivity(intent);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }

                    new Handler().postDelayed(new Runnable() {
                                                  public void run() {
                                                      WidgetContainerActivity.this.finish();
                                                  }
                                              }, 3000
                    );
                    break;

                }
                case PICK_CONTACT: {

                    Contacts contacts = Contacts.getInstance(this);
                    String id = data.getData().getLastPathSegment();
                    String number = contacts.getNumberByID(id);
                    String name = contacts.getNameByNumber(number);
                    PhoneNumber phoneNumber = new PhoneNumber(number);
                    number = phoneNumber.getNumber();
                    number = number.replace(" ", "");

                    final ImageView contact_image = (ImageView) findViewById(R.id.contact_image);

                    //get photoUri
                    Uri displayPhotoUri = contacts.getUriDisplayPhoto(number);

                    final int dim = (int) getResources().getDimension(R.dimen.avatar_size);

                    //insert image on imageView
                    if (displayPhotoUri != null){

                        Picasso.with(this)
                                .load(displayPhotoUri)
                                .resize(dim, dim)
                                .transform(MainActivity.transformation)
                                .into(contact_image, this);
                    }
                    else {
                        Picasso.with(this)
                                .load(R.drawable.user)
                                .resize(dim, dim)
                                .transform(MainActivity.transformation)
                                .into(contact_image);
                    }

                    TextView contactName = (TextView) findViewById(R.id.contact_name);
                    contactName.setText(name);

                    EditText editText = (AutoCompleteTextView) findViewById(R.id.name_or_number);
                    editText.setText(number);
                    break;

                }
                default: {
                    finish();
                }
            }
        }
        else
            finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.contact_image){

            Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(it, PICK_CONTACT);

        }

    }

    //Callback del Picasso
    @Override
    public void onSuccess() {}

    //Callback del Picasso
    @Override
    public void onError() {
        ImageView contact_image = (ImageView) findViewById(R.id.contact_image);
        final int dim = (int) getResources().getDimension(R.dimen.avatar_size);

        Picasso.with(this)
                .load(R.drawable.user)
                .resize(dim, dim)
                .transform(MainActivity.transformation)
                .into(contact_image);
    }

    private class mTextWatcher implements TextWatcher {

        protected TextView contact_name;
        protected ImageView contact_image;
        protected int dim;
        protected Contacts contacts;

        public mTextWatcher(TextView contact_name, ImageView contact_image, Contacts contacts) {
            this.contact_name = contact_name;
            this.contact_image = contact_image;
            this.contacts = contacts;
            this.dim = (int) getResources().getDimension(R.dimen.avatar_size);
        }

        public void defaultValues(){
            contact_name.setText(WidgetContainerActivity.this.getResources().getString(R.string.receiver));
            Picasso.with(WidgetContainerActivity.this)
                    .load(R.drawable.user)
                    .resize(dim, dim)
                    .transform(MainActivity.transformation)
                    .into(contact_image);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String cadena = charSequence.toString();
            if (PhoneNumber.isValidNumber(cadena)){
                String name = contacts.getNameByNumber(cadena);
                Uri imageUri = contacts.getUriDisplayPhoto(cadena);

                if (imageUri != null){

                    Picasso.with(WidgetContainerActivity.this)
                            .load(imageUri)
                            .resize(dim, dim)
                            .transform(MainActivity.transformation)
                            .into(contact_image, WidgetContainerActivity.this);

                }
                if (name == null)
                    defaultValues();
                else
                    contact_name.setText(name);

            }
            else
                defaultValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }

    }
}
