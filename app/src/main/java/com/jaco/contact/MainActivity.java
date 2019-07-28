package com.jaco.contact;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaco.contact.errorReports.LogWriter;
import com.jaco.contact.errorReports.UncaughtException;
import com.jaco.contact.preferences.Preferences;
import com.jaco.contact.preferences.PreferencesActivity;
import com.jaco.contact.preferences.mSharedPreferences;
import com.jaco.contact.service.CallListenerService;
import com.jaco.headerrecyclerview.RecyclerViewHeaderAdapter;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ServiciosSmsFragment.OnServiciosSmsInteractionListener, Callback, OnClickListener, StartFragment.OnStartFragmentInteractionListener {

    private static final int[] menu_items = new int[]{R.string.birthdays, R.string.call_log, R.string.advanced_search,
            R.string.settings, R.string.about};
    private static final int[] menu_summary = new int[]{R.string.birthdays_summary, R.string.call_log_summary, R.string.advanced_search_summary,
            R.string.settings_summary, R.string.about_summary};
    private static final int[] menu_icons = new int[]{
            R.drawable.icon_birthday,
            R.drawable.icon_call_log,
            R.drawable.icon_search,
            R.drawable.icon_settings,
            R.drawable.icon_help};

    private static final int SPLASH_ACTIVITY = 123;

    private static final String ADVANCED_SEARCH_FRAGMENT = "advanced_search";
    private static final String BIRTHDAY_FRAGMENT = "birthday";
    private static final String CALL_LOG_FRAGMENT = "call_log";
    private static final String START_FRAGMENT = "start_fragment";

    private static final int PICK_CONTACT_FREE = 1;
    private static final int OPTIONS_MENU = 2;
    private static final int PERMISSION_REQUEST = 4532;

    private boolean close;

    private MenuAdapter mMenuAdapter;
    private DrawerLayout mDrawerLayout;
    private RecyclerView mMenu;

    //create transformation
    public static final Transformation transformation = new RoundedTransformationBuilder()
            .cornerRadiusDp(90)
            .oval(false)
            .build();

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions(){

        String[] permissions = new String[7];
        int permissionCount = 0;

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            permissions[permissionCount++] = Manifest.permission.READ_CONTACTS;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            permissions[permissionCount++] = Manifest.permission.CALL_PHONE;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            permissions[permissionCount++] = Manifest.permission.READ_CALL_LOG;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            permissions[permissionCount++] = Manifest.permission.SEND_SMS;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissions[permissionCount++] = Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissions[permissionCount++] = Manifest.permission.CAMERA;
        }

        String[] perms = new String[permissionCount];
        System.arraycopy(permissions, 0, perms, 0, permissionCount);

        if (permissionCount >= 1) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    perms, PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //start splash
//        Intent splash = new Intent(this, SplashActivity.class);
//        startActivityForResult(splash, SPLASH_ACTIVITY);

        checkPermissions();

        UncaughtException.startCrashReporter(this);

//        si es android >= 5.0 se debe permitir overlay
//        este permiso se solicitara al activar las notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
//            no mostrar notificaciones
            mSharedPreferences.setShowAlert(this, 4);

        if (mSharedPreferences.isFirstTimeOpen(this) || mSharedPreferences.getDatabasePath(this).length() == 0){
            mSharedPreferences.setFirstTimeOpen(this, false);
            new FindDatabase(this).execute();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        close = false;

        final EditText find = (AutoCompleteTextView) findViewById(R.id.name_or_number);
        new GetContacts(this).execute();

        LinearLayout find_button = (LinearLayout) findViewById(R.id.find_button);
        find_button.setOnClickListener(this);

        final ImageView contact_image = (ImageView) findViewById(R.id.contact_image);
        contact_image.setOnClickListener(this);

        final int dim = (int) getResources().getDimension(R.dimen.avatar_size);
        Picasso.with(this)
                .load(R.drawable.user)
                .resize(dim, dim)
                .transform(transformation)
                .into(contact_image);

//        contact_image.setImageBitmap(Utils.circleBitmap(this, R.drawable.user));

        find.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                final String name_or_number = ((EditText)MainActivity.this.findViewById(R.id.name_or_number)).getText().toString();
                if (name_or_number.length() != 0)
                    findContact(name_or_number);
                return true;
            }
        });

        final Contacts contacts = Contacts.getInstance(this);

        TextView contact_name = (TextView) MainActivity.this.findViewById(R.id.contact_name);
        find.addTextChangedListener(new mTextWatcher(
                contact_name, contact_image, contacts
        ));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                    .replace(R.id.container, StartFragment.newInstance(), START_FRAGMENT)
                    .addToBackStack(null)
                    .commit();
        }

//        createDrawer(toolbar);

        if (LogWriter.hasErrorLog(this)){
            errorMensaje();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            startService(new Intent(this, CallListenerService.class));
        }

    }

    private void errorMensaje(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle(R.string.error_alert)
                .setMessage(R.string.error_alert_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.send_report, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LogWriter.sendErrorReport(MainActivity.this);
                        LogWriter.getLogFile(MainActivity.this).delete();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LogWriter.getLogFile(MainActivity.this).delete();
                    }
                })
                .setCancelable(false);

        dialog.show();

    }

    public void createDrawer(Toolbar toolbar){

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mMenu = (RecyclerView) mDrawerLayout.findViewById(R.id.menu_recycler);

        View header = getLayoutInflater().inflate(R.layout.drawer_header, null);

        mMenuAdapter = new MenuAdapter(this, menu_items, menu_summary, menu_icons);
        mMenuAdapter.setHeader(header);
        mMenuAdapter.setHeaderHeight((int) (150 * displayMetrics.density));
        mMenuAdapter.setSingleChoice(true);
        mMenuAdapter.setOnItemClickListener(new MenuItemClick());

        mMenuAdapter.setItemSelectable(R.string.settings, false);
        mMenuAdapter.setItemSelectable(R.string.about, false);

        mMenu.setAdapter(mMenuAdapter);

        //create toggle and sync
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        View drawer_border = mDrawerLayout.findViewById(R.id.drawer_border);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            drawer_border.setBackgroundDrawable(new BitmapDrawable(getResources(), Utils.drawerBorderColored(this)));
        }
        else {
            drawer_border.setBackground(new BitmapDrawable(getResources(), Utils.drawerBorderColored(this)));
        }
        drawer_border.setClickable(true);

    }

    public void onMenuItemSelected(int i){

        int _id = mMenuAdapter.getItem(i).getId();

        switch (_id){
            case R.string.settings: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                    startActivity(new Intent(this, PreferencesActivity.class));
                else {
                    startActivity(new Intent(MainActivity.this, Preferences.class));
                }

                break;
            }
            case R.string.birthdays :{
                String currentName = getCurrentFragmentName();

                if (currentName == null || !currentName.equals(BIRTHDAY_FRAGMENT)) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, BirthdayFragment.newInstance())
                            .addToBackStack(BIRTHDAY_FRAGMENT)
                            .commit();
                }
                break;
            }
            case R.string.advanced_search: {
                String currentName = getCurrentFragmentName();

                if (currentName == null || !currentName.equals(ADVANCED_SEARCH_FRAGMENT)) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, AdvancedSearchFragment.newInstance())
                            .addToBackStack(ADVANCED_SEARCH_FRAGMENT)
                            .commit();
                }
                break;
            }
            case R.string.call_log: {
                String currentName = getCurrentFragmentName();

                if (currentName == null || !currentName.equals(CALL_LOG_FRAGMENT)){
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, CallLogFragment.newInstance())
                            .addToBackStack(CALL_LOG_FRAGMENT)
                            .commit();
                }
                break;
            }
            case R.string.about: {
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
                break;
            }
        }

        mDrawerLayout.closeDrawers();

    }

    @Override
    public void onBackPressed() {

//        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)){
//            mDrawerLayout.closeDrawer(GravityCompat.START);
//            return;
//        }

        if (close)
            finish();

        switch (getSupportFragmentManager().getBackStackEntryCount()){
            case 1: {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(START_FRAGMENT);
                if (fragment != null && fragment.isVisible()) {

                    if (!close)
                        Toast.makeText(this, R.string.one_more_time, Toast.LENGTH_SHORT).show();

                    close = true;

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            close = false;
                        }
                    }, 3000);

                }
                else {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, StartFragment.newInstance(), START_FRAGMENT)
                            .addToBackStack(null)
                            .commit();

//                    mMenuAdapter.clearSelection();
                }

                break;
            }
            default: {
                super.onBackPressed();
            }

        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.find_button: {

                final String name_or_number = ((EditText)MainActivity.this.findViewById(R.id.name_or_number)).getText().toString();
                if (name_or_number.length() != 0)
                    findContact(name_or_number);

                break;
            }
            case R.id.contact_image: {
                Intent it = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(it, PICK_CONTACT_FREE);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST: {

                int permCount = 0;

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                        permCount++;
                }

                if (permCount != 0 && mSharedPreferences.showPermissionsAlert(this)){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                    dialog.setTitle(R.string.permission_dialog_title)
                            .setMessage(R.string.permission_dialog_text)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNeutralButton(R.string.not_remember, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                        mSharedPreferences.setPermissionsAlert(MainActivity.this, false);
                                    }
                                })
                            .setPositiveButton(R.string.accept, null);

                    dialog.show();
                }

                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    String name = contacts.getNameByNumber(number);
                    number = new ContactDetail(name, number).getNumber();

                    final ImageView contact_image = (ImageView) findViewById(R.id.contact_image);

                    //get photoUri
                    Uri displayPhotoUri = contacts.getUriDisplayPhoto(number);

                    final int dim = (int) getResources().getDimension(R.dimen.avatar_size);

                    //insert image on imageView
                    Bitmap bm = null;
                    if (displayPhotoUri != null){

//                        bm = Utils.circleBitmap(this, displayPhotoUri);

                        Picasso.with(this)
                                .load(displayPhotoUri)
                                .resize(dim, dim)
                                .transform(transformation)
                                .into(contact_image, this);
                    }
                    else {
                        Picasso.with(this)
                                .load(R.drawable.user)
                                .resize(dim, dim)
                                .transform(transformation)
                                .into(contact_image);
                    }

//                    if (bm != null)
//                        contact_image.setImageBitmap(bm);
//                    else
//                        contact_image.setImageBitmap(Utils.circleBitmap(this, R.drawable.user));

                    TextView contactName = (TextView) findViewById(R.id.contact_name);
                    contactName.setText(name);

                    EditText editText = (AutoCompleteTextView) findViewById(R.id.name_or_number);
                    editText.setText(number);

                    break;
                }
                case OPTIONS_MENU: {
                    int resource_id = data.getIntExtra(MenuActivity.ITEM_CLICK_ID, -1);
                    onCustomMenuItemSelected(resource_id);
                    break;
                }
            }

        }
    }

    public void showOptionMenu(){
        Intent intent = new Intent(this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, OPTIONS_MENU);
    }

    public void onCustomMenuItemSelected(int _id){

        switch (_id){
            case R.id.action_settings: {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
                    startActivity(new Intent(this, PreferencesActivity.class));
                else {
                    startActivity(new Intent(MainActivity.this, Preferences.class));
                }

                break;
            }
            case R.id.action_birthday: {
                String currentName = getCurrentFragmentName();

                if (currentName == null || !currentName.equals(BIRTHDAY_FRAGMENT)) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, BirthdayFragment.newInstance())
                            .addToBackStack(BIRTHDAY_FRAGMENT)
                            .commit();
                }
                break;
            }
            case R.id.action_advanced_search: {
                String currentName = getCurrentFragmentName();

                if (currentName == null || !currentName.equals(ADVANCED_SEARCH_FRAGMENT)) {
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, AdvancedSearchFragment.newInstance())
                            .addToBackStack(ADVANCED_SEARCH_FRAGMENT)
                            .commit();
                }
                break;
            }
            case R.id.action_call_log: {
                String currentName = getCurrentFragmentName();

                if (currentName == null || !currentName.equals(CALL_LOG_FRAGMENT)){
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                            .replace(R.id.container, CallLogFragment.newInstance())
                            .addToBackStack(CALL_LOG_FRAGMENT)
                            .commit();
                }
                break;
            }
            case R.id.action_about: {
                startActivity(new Intent(MainActivity.this, ContactActivity.class));
            }
        }

    }

    public void findContact(String name_or_number){
        //verificar la base de datos
        EtecsaDB database = new EtecsaDB(this);
        if (!database.hasDatabase()){
            Toast.makeText(this, R.string.no_database, Toast.LENGTH_SHORT).show();
        }

        //buscar contacto
        if (database.hasDatabase() && !PhoneNumber.isValidNumber(name_or_number))
            createFilterDialog(name_or_number);

        if (database.hasDatabase() && PhoneNumber.isValidNumber(name_or_number))
            new Search(MainActivity.this, null).execute(name_or_number);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode){
            case KeyEvent.KEYCODE_MENU: {
                showOptionMenu();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_options: {
                showOptionMenu();
                return true;
            }
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get current fragment name or null if there are not fragments on FragmentBackStack
     * @return fragment name
     */
    public String getCurrentFragmentName(){
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count != 0)
            return getSupportFragmentManager().getBackStackEntryAt(count-1).getName();
        return null;
    }

    public void createFilterDialog(final String name_or_number){
        AlertDialog.Builder filter_dialog = new AlertDialog.Builder(MainActivity.this);
        filter_dialog.setTitle(R.string.filter);
        filter_dialog.setMessage(R.string.filter_message);
        filter_dialog.setPositiveButton(R.string.mobile, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Search(MainActivity.this, PhoneType.MOVIL).execute(name_or_number);
            }
        });

        filter_dialog.setNeutralButton(R.string.both, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Search(MainActivity.this, null).execute(name_or_number);
            }
        });

        filter_dialog.setNegativeButton(R.string.fix, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new Search(MainActivity.this, PhoneType.FIX).execute(name_or_number);
            }
        });

        filter_dialog.show();
    }

    //Callback del Picasso
    @Override
    public void onSuccess() {}

    //Callback del Picasso
    @Override
    public void onError() {
        ImageView contact_image = (ImageView) findViewById(R.id.contact_image);
        final int dim = (int) getResources().getDimension(R.dimen.avatar_size);

        Picasso.with(MainActivity.this)
                .load(R.drawable.user)
                .resize(dim, dim)
                .transform(transformation)
                .into(contact_image);

//        contact_image.setImageBitmap(Utils.circleBitmap(this, R.drawable.user));
    }

    @Override
    public void onStartFragmentInteraction(int id) {
        switch (id){
            case R.id.button_saldo: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(R.id.container, CreditFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.button_transfer: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(R.id.container, TransferFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.button_buy_all: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(R.id.container, BuyFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                break;
            }
            case R.id.button_check_imei: {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(R.id.container, CheckImei.newInstance())
                        .addToBackStack(null)
                        .commit();
                break;
            }
        }
    }

    @Override
    public void onServiciosSmsInteraction(int id) {

        switch (id) {
            case R.id.button_sms_entumovil: {

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(R.id.container, EntumovilFragment.newInstance())
                        .addToBackStack(null)
                        .commit();

                break;
            }
        }

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
            contact_name.setText(MainActivity.this.getResources().getString(R.string.receiver));

//            contact_image.setImageBitmap(Utils.circleBitmap(MainActivity.this, R.drawable.user));

            Picasso.with(MainActivity.this)
                    .load(R.drawable.user)
                    .resize(dim, dim)
                    .transform(transformation)
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

//                Bitmap bm = null;
                if (imageUri != null){

//                    bm = Utils.circleBitmap(MainActivity.this, imageUri);

                    Picasso.with(MainActivity.this)
                            .load(imageUri)
                            .resize(dim, dim)
                            .transform(transformation)
                            .into(contact_image, MainActivity.this);

                }

//                if (bm != null)
//                    contact_image.setImageBitmap(bm);
//                else
//                    contact_image.setImageBitmap(Utils.circleBitmap(MainActivity.this, R.drawable.user));

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

    private class Search extends AsyncTask<String, Void, PhoneEntry[]> {

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;
        protected PhoneType phoneType;

        public Search(Activity activity, PhoneType phoneType) {
            this.weakReference = new WeakReference<>(activity);
            this.phoneType = phoneType;
        }

        @Override
        protected void onPreExecute() {

            final Activity activity = weakReference.get();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog = ProgressDialog.show(activity, "",
                            activity.getString(R.string.searching), true);
                }
            });

        }

        @Override
        protected PhoneEntry[] doInBackground(String... numbers) {

            String name_or_number = numbers[0];
            PhoneNumber phoneNumber = new PhoneNumber(name_or_number);

            //Abrir la conexion a la base de datos
            EtecsaDB etecsaDB = new EtecsaDB(weakReference.get());
            try {
                etecsaDB.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            PhoneEntry phoneEntry[];
            if (PhoneNumber.isValidNumber(name_or_number)){
                phoneEntry = etecsaDB.searchByNumber(phoneNumber.getNumber());
            }
            else if (phoneType != null && phoneType == PhoneType.FIX){
                phoneEntry = etecsaDB.searchFixByName(name_or_number);
            }
            else if (phoneType != null && phoneType == PhoneType.MOVIL) {
                phoneEntry = etecsaDB.searchMobileByName(name_or_number);
            }
            else {
                phoneEntry = etecsaDB.searchByName(name_or_number);
            }

            //cerrar la conexion a la base de datos
            etecsaDB.close();

            return phoneEntry;
        }

        @Override
        protected void onPostExecute(PhoneEntry[] phoneEntry) {

            if (phoneEntry != null && phoneEntry.length > 1){
                //encontro algo entonces abrir el nuevo fragment
                AppCompatActivity activity = (AppCompatActivity) weakReference.get();

                activity.getSupportFragmentManager().popBackStack();

                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .addToBackStack(null)
                        .replace(R.id.container, PhonesListFragment.newInstance(phoneEntry))
                        .commit();
            }
            else if (phoneEntry != null && phoneEntry.length == 1){
                AppCompatActivity activity = (AppCompatActivity) weakReference.get();
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.PHONE_ENTRY, phoneEntry[0]);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                activity.startActivity(intent);
            }
            else {
                //no encontro nada mostrar mensaje
                weakReference.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(weakReference.get().getApplicationContext(), R.string.not_matches, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            progressDialog.dismiss();
        }
    }

    public static class GetContacts extends AsyncTask<Void, Void, List<ContactDetail>> {

        protected WeakReference<Activity> weakReference;
        protected long startTime;

        public GetContacts(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
            startTime = -1;
        }

        @Override
        protected List<ContactDetail> doInBackground(Void... voids) {
            startTime = new GregorianCalendar().getTimeInMillis();
            return Contacts.getInstance(weakReference.get()).getContacts();
        }

        @Override
        protected void onPostExecute(List<ContactDetail> contactDetails) {

            long currentTime = new GregorianCalendar().getTimeInMillis();

            closeSplash(
                    currentTime - startTime < 3000 ? 3000 - (currentTime - startTime) : 1
            );

            AutoCompleteTextView autocomplete = (AutoCompleteTextView)weakReference.get().findViewById(R.id.name_or_number);
            autocomplete.setAdapter(new AutocompleteAdapter(weakReference.get(), contactDetails));
        }

        protected void closeSplash(long delayTime){

            final Activity activity = weakReference.get();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.finishActivity(SPLASH_ACTIVITY);
                }
            }, delayTime);

        }
    }

    public static class FindDatabase extends AsyncTask<Void, Void, Boolean> {

        protected WeakReference<Activity> weakReference;

        public FindDatabase(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... aVoids) {
            Map<String, Integer> scanned = new HashMap<>();

            File external = Environment.getExternalStorageDirectory();
            scanned.put(external.getAbsolutePath(), 1);
            boolean found = Utils.selectDatabase(weakReference.get(), external);

            //probar con rutas alternativas
            external = new File("/storage/emulated/0");
            if (!found && external.exists() && external.isDirectory() && scanned.get(external.getAbsolutePath()) == null)
                found = Utils.selectDatabase(weakReference.get(), external);
            scanned.put(external.getAbsolutePath(), 1);

            external = new File("/storage");
            if (!found && external.exists() && external.isDirectory() && scanned.get(external.getAbsolutePath()) == null)
                found = Utils.selectDatabase(weakReference.get(), external);
            scanned.put(external.getAbsolutePath(), 1);

            external = new File("/sdcard");
            if (!found && external.exists() && external.isDirectory() && scanned.get(external.getAbsolutePath()) == null)
                found = Utils.selectDatabase(weakReference.get(), external);
            scanned.put(external.getAbsolutePath(), 1);

            external = new File("/mnt");
            if (!found && external.exists() && external.isDirectory() && scanned.get(external.getAbsolutePath()) == null)
                found = Utils.selectDatabase(weakReference.get(), external);
            scanned.put(external.getAbsolutePath(), 1);

            return found;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean){
                Toast.makeText(weakReference.get(),
                        String.format(weakReference.get().getResources().getString(R.string.database_found), mSharedPreferences.getDatabasePath(weakReference.get())),
                        Toast.LENGTH_LONG)
                    .show();
            }
            else {
                Toast.makeText(weakReference.get(),
                        R.string.database_not_found,
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    public class MenuItemClick implements RecyclerViewHeaderAdapter.OnItemClickListener {

        @Override
        public void onItemClick(RecyclerView.ViewHolder viewHolder, int i) {
            onMenuItemSelected(i);
        }
    }

}
