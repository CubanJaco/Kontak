package com.jaco.contact;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by osvel on 7/25/16.
 */
public class AdvancedSearchFragment extends Fragment implements View.OnClickListener {

    private AdvancedSearch advancedSearch;
//    private PhoneType type;

    public AdvancedSearchFragment() {}

    public static Fragment newInstance(){
        AdvancedSearchFragment fragment = new AdvancedSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootview = inflater.inflate(R.layout.fragment_advanced_search, container, false);

        advancedSearch = new AdvancedSearch();

        LinearLayout button_number = (LinearLayout) rootview.findViewById(R.id.button_number);
        button_number.setOnClickListener(this);

        LinearLayout button_name = (LinearLayout) rootview.findViewById(R.id.button_name);
        button_name.setOnClickListener(this);

        LinearLayout button_identification = (LinearLayout) rootview.findViewById(R.id.button_CI);
        button_identification.setOnClickListener(this);

        LinearLayout button_province = (LinearLayout) rootview.findViewById(R.id.button_province);
        button_province.setOnClickListener(this);

        LinearLayout button_find = (LinearLayout) rootview.findViewById(R.id.button_find);
        button_find.setOnClickListener(this);

        LinearLayout switch_button = (LinearLayout) rootview.findViewById(R.id.switch_layout);
        switch_button.setOnClickListener(this);

        LinearLayout fix_switch = (LinearLayout) switch_button.findViewById(R.id.fix_switch);
        LinearLayout mobile_switch = (LinearLayout) switch_button.findViewById(R.id.mobile_switch);

        if (fix_switch.getVisibility() == View.VISIBLE && mobile_switch.getVisibility() == View.INVISIBLE) {
            advancedSearch.setType(PhoneType.FIX);
            button_identification.setVisibility(View.GONE);
        }
        else {
            fix_switch.setVisibility(View.INVISIBLE);
            mobile_switch.setVisibility(View.VISIBLE);
            button_identification.setVisibility(View.VISIBLE);
            advancedSearch.setType(PhoneType.MOVIL);
        }

        return rootview;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id){
            case R.id.button_number: {
                createNumberDialog();
                break;
            }
            case R.id.button_name: {
                createNameDialog();
                break;
            }
            case R.id.button_CI: {
                createIdentificationDialog();
                break;
            }
            case R.id.button_province: {
                createProvinceDialog();
                break;
            }
            case R.id.button_find: {
                if (advancedSearch.hasValues())
                    findContact();
                else
                    Toast.makeText(getActivity(), R.string.null_search, Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.switch_layout: {
                advancedSearch.setType(
                        animateSwitch((LinearLayout) view));
                LinearLayout button_identification = (LinearLayout) getActivity().findViewById(R.id.button_CI);
                if (advancedSearch.getType() == PhoneType.FIX){
                    advancedSearch.resetValues(AdvancedSearch.IDENTIFICATION_INDEX);
                    button_identification.setVisibility(View.GONE);
                }
                else {
                    button_identification.setVisibility(View.VISIBLE);
                }
                break;
            }
        }
    }

    public PhoneType animateSwitch(LinearLayout switch_layout){

        LinearLayout fix_switch = (LinearLayout) switch_layout.findViewById(R.id.fix_switch);
        LinearLayout mobile_switch = (LinearLayout) switch_layout.findViewById(R.id.mobile_switch);

        if (fix_switch.getVisibility() == View.INVISIBLE){

            Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.switch_enter_rigth_to_left);
            fix_switch.startAnimation(animation);
            fix_switch.setVisibility(View.VISIBLE);

            animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.switch_exit_rigth_to_left);
            mobile_switch.startAnimation(animation);
            mobile_switch.setVisibility(View.INVISIBLE);

            return PhoneType.FIX;
        }
        else {

            Animation animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.switch_exit_left_to_right);
            fix_switch.startAnimation(animation);
            fix_switch.setVisibility(View.INVISIBLE);

            animation = AnimationUtils.loadAnimation(this.getContext(), R.anim.switch_enter_left_to_right);
            mobile_switch.startAnimation(animation);
            mobile_switch.setVisibility(View.VISIBLE);

            return PhoneType.MOVIL;
        }

    }

    public void findContact(){
        //verificar la base de datos
        EtecsaDB database = new EtecsaDB(this.getContext());
        if (!database.hasDatabase()){
            Toast.makeText(this.getContext(), R.string.no_database, Toast.LENGTH_SHORT).show();
        }
        else //buscar contacto
            new Search(this.getActivity()).execute(advancedSearch);
    }

    public void createNumberDialog(){

        AlertDialog.Builder number_dialog = new AlertDialog.Builder(getActivity());
        final View dialog_view = getActivity().getLayoutInflater().inflate(R.layout.advanced_numeric_dialog, null);
        final TextView number = (TextView) getActivity().findViewById(R.id.number);
        final TextView number_description = (TextView) getActivity().findViewById(R.id.number_description);

        //restore values
        EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
        editText.setText(advancedSearch.get(AdvancedSearch.NUMBER_EXACTLY_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.start);
        editText.setText(advancedSearch.get(AdvancedSearch.NUMBER_START_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.contains);
        editText.setText(advancedSearch.get(AdvancedSearch.NUMBER_CONTAINS_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.ends);
        editText.setText(advancedSearch.get(AdvancedSearch.NUMBER_END_INDEX));

        number_dialog.setView(dialog_view);
        number_dialog.setTitle(R.string.number_filter);
        number_dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //save values
                EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
                advancedSearch.set(AdvancedSearch.NUMBER_EXACTLY_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.start);
                advancedSearch.set(AdvancedSearch.NUMBER_START_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.contains);
                advancedSearch.set(AdvancedSearch.NUMBER_CONTAINS_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.ends);
                advancedSearch.set(AdvancedSearch.NUMBER_END_INDEX, editText.getText().toString());

                String number_string = advancedSearch.getString(AdvancedSearch.NUMBER_INDEX);
                if (number_string.length() != 0) {
                    number.setVisibility(View.VISIBLE);
                    number_description.setText(number_string);
                }
                else {
                    number.setVisibility(View.INVISIBLE);
                    number_description.setText(number.getText());
                }

            }
        });
        number_dialog.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //reset values
                EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.start);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.contains);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.ends);
                editText.setText("");

                advancedSearch.resetValues(AdvancedSearch.NUMBER_INDEX);

                number.setVisibility(View.INVISIBLE);
                number_description.setText(number.getText());
            }
        });
        number_dialog.setNegativeButton(R.string.cancel, null);
        number_dialog.show();
    }

    public void createNameDialog(){

        AlertDialog.Builder name_dialog = new AlertDialog.Builder(getActivity());
        final View dialog_view = getActivity().getLayoutInflater().inflate(R.layout.advanced_dialog, null);
        final TextView name = (TextView) getActivity().findViewById(R.id.name);
        final TextView name_description = (TextView) getActivity().findViewById(R.id.name_description);

        //restore values
        EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
        editText.setText(advancedSearch.get(AdvancedSearch.NAME_EXACTLY_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.start);
        editText.setText(advancedSearch.get(AdvancedSearch.NAME_START_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.contains);
        editText.setText(advancedSearch.get(AdvancedSearch.NAME_CONTAINS_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.ends);
        editText.setText(advancedSearch.get(AdvancedSearch.NAME_END_INDEX));

        name_dialog.setView(dialog_view);
        name_dialog.setTitle(R.string.name_filter);
        name_dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //save values
                EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
                advancedSearch.set(AdvancedSearch.NAME_EXACTLY_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.start);
                advancedSearch.set(AdvancedSearch.NAME_START_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.contains);
                advancedSearch.set(AdvancedSearch.NAME_CONTAINS_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.ends);
                advancedSearch.set(AdvancedSearch.NAME_END_INDEX, editText.getText().toString());

                String name_string = advancedSearch.getString(AdvancedSearch.NAME_INDEX);
                if (name_string.length() != 0){
                    name.setVisibility(View.VISIBLE);
                    name_description.setText(name_string);
                }
                else {
                    name.setVisibility(View.INVISIBLE);
                    name_description.setText(name.getText());
                }

            }
        });
        name_dialog.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //reset values
                EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.start);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.contains);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.ends);
                editText.setText("");

                advancedSearch.resetValues(AdvancedSearch.NAME_INDEX);

                name.setVisibility(View.INVISIBLE);
                name_description.setText(name.getText());
            }
        });
        name_dialog.setNegativeButton(R.string.cancel, null);
        name_dialog.show();
    }

    public void createIdentificationDialog(){

        final AlertDialog.Builder identification_dialog = new AlertDialog.Builder(getActivity());
        final View dialog_view = getActivity().getLayoutInflater().inflate(R.layout.advanced_numeric_dialog, null);
        final TextView identification = (TextView) getActivity().findViewById(R.id.CI);
        final TextView identification_description = (TextView) getActivity().findViewById(R.id.CI_description);

        //restore values
        EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
        editText.setText(advancedSearch.get(AdvancedSearch.IDENTIFICATION_EXACTLY_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.start);
        editText.setText(advancedSearch.get(AdvancedSearch.IDENTIFICATION_START_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.contains);
        editText.setText(advancedSearch.get(AdvancedSearch.IDENTIFICATION_CONTAINS_INDEX));

        editText = (EditText) dialog_view.findViewById(R.id.ends);
        editText.setText(advancedSearch.get(AdvancedSearch.IDENTIFICATION_END_INDEX));

        identification_dialog.setView(dialog_view);
        identification_dialog.setTitle(R.string.identification_filter);
        identification_dialog.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //save values
                EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
                advancedSearch.set(AdvancedSearch.IDENTIFICATION_EXACTLY_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.start);
                advancedSearch.set(AdvancedSearch.IDENTIFICATION_START_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.contains);
                advancedSearch.set(AdvancedSearch.IDENTIFICATION_CONTAINS_INDEX, editText.getText().toString());

                editText = (EditText) dialog_view.findViewById(R.id.ends);
                advancedSearch.set(AdvancedSearch.IDENTIFICATION_END_INDEX, editText.getText().toString());

                String identification_string = advancedSearch.getString(AdvancedSearch.IDENTIFICATION_INDEX);
                if (identification_string.length() != 0) {
                    identification.setVisibility(View.VISIBLE);
                    identification_description.setText(identification_string);
                }
                else {
                    identification.setVisibility(View.INVISIBLE);
                    identification_description.setText(identification.getText());
                }
            }
        });
        identification_dialog.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //reset values
                EditText editText = (EditText) dialog_view.findViewById(R.id.exactly);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.start);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.contains);
                editText.setText("");

                editText = (EditText) dialog_view.findViewById(R.id.ends);
                editText.setText("");

                advancedSearch.resetValues(AdvancedSearch.IDENTIFICATION_INDEX);

                identification.setVisibility(View.INVISIBLE);
                identification_description.setText(identification.getText());
            }
        });
        identification_dialog.setNegativeButton(R.string.cancel, null);
        identification_dialog.show();
    }

    public void createProvinceDialog(){

        Activity activity = AdvancedSearchFragment.this.getActivity();
        final TextView province = (TextView) activity.findViewById(R.id.province);
        final TextView province_description = (TextView) activity.findViewById(R.id.province_description);
        final String[] provinces = activity.getResources().getStringArray(R.array.provinces_list);
        final int[] province_codes = activity.getResources().getIntArray(R.array.province_codes);

        AlertDialog.Builder province_dialog = new AlertDialog.Builder(getActivity());
        province_dialog.setItems(getActivity().getResources().getStringArray(R.array.provinces_list), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                advancedSearch.set(AdvancedSearch.PROVINCE_CODE, province_codes[i]);
                province.setVisibility(View.VISIBLE);
                province_description.setText(provinces[i]);
            }
        });
        province_dialog.setTitle(R.string.province_filter);

        province_dialog.setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                province.setVisibility(View.INVISIBLE);
                province_description.setText(province.getText());
                advancedSearch.resetValues(AdvancedSearch.PROVINCE_INDEX);
            }
        });

        province_dialog.setNegativeButton(R.string.cancel, null);

        province_dialog.show();

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
    public void onStop() {
        super.onStop();

        //mostrar campo de busqueda
//        LinearLayout search_layout = (LinearLayout) getActivity().findViewById(R.id.search_layout);
//        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_up);
//        search_layout.startAnimation(animation);
//        search_layout.setVisibility(View.VISIBLE);
    }

    private class Search extends AsyncTask<AdvancedSearch, Void, PhoneEntry[]> {

        protected WeakReference<Activity> weakReference;
        protected ProgressDialog progressDialog;

        public Search(Activity activity) {
            this.weakReference = new WeakReference<>(activity);
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
        protected PhoneEntry[] doInBackground(AdvancedSearch... advancedSearches) {

            EtecsaDB db = new EtecsaDB(weakReference.get());
            try {
                db.open();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            PhoneEntry phoneEntry[] = null;
            if (db.isOpen())
                phoneEntry = db.advancedSearch(advancedSearches[0]);

            //cerrar la conexion a la base de datos
            db.close();

            return phoneEntry;
        }

        @Override
        protected void onPostExecute(PhoneEntry[] phoneEntry) {

            if (phoneEntry != null && phoneEntry.length > 1){
                //encontro algo entonces abrir el nuevo fragment
                AppCompatActivity activity = (AppCompatActivity) weakReference.get();
                activity.getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .addToBackStack(null)
                        .replace(R.id.container, PhonesListFragment.newInstance(phoneEntry))
                        .commit();
            }
            else if (phoneEntry != null && phoneEntry.length == 1){
                AppCompatActivity activity = (AppCompatActivity) weakReference.get();
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.PHONE_ENTRY, phoneEntry[0]);
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
}
