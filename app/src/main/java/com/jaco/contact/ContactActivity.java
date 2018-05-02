package com.jaco.contact;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String versionName;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "2.0+";
        }

        TextView about_text = (TextView) findViewById(R.id.about_text);
        about_text.setText(
                getResources().getText(R.string.contact_version)
                + versionName
                + getResources().getText(R.string.contact_copyright)
                + getResources().getText(R.string.contact_email)
                + getResources().getText(R.string.contact_number)
                + getResources().getText(R.string.large_text)
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildTransferDialog();
            }
        });
    }

    public void buildTransferDialog(){

        final String contact_number =
                new PhoneNumber(getResources().getString(R.string.contact_number))
                    .getNumber();

        AlertDialog.Builder transfer_dialog = new AlertDialog.Builder(this);
        View dialog_view = getLayoutInflater().inflate(R.layout.transfer_dialog, null);

        transfer_dialog.setTitle(R.string.donate);
        transfer_dialog.setView(dialog_view);

        final EditText pincode = (EditText) dialog_view.findViewById(R.id.pin_code);
        final EditText transfer = (EditText) dialog_view.findViewById(R.id.transfer);

        transfer_dialog.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String pin = pincode.getText().toString();
                String monto = transfer.getText().toString();
                transfer(contact_number, pin, monto);
            }
        });

        transfer_dialog.setNegativeButton(R.string.cancel, null);

        transfer_dialog.show();


    }

    private void transfer(String number, String pin, String monto){
        String transfer_code = "*234*1*";

        if (pin.length() == 4 && PhoneNumber.isValidNumber(number) && monto.length() == 0){
            //transferencia con centavos
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + Uri.encode(transfer_code + number + "*" + pin + "#")));
            try {
                startActivity(intent);
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
        }
        else if (pin.length() == 4 && PhoneNumber.isValidNumber(number)) {
            //transferencia sin centavos
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + Uri.encode(transfer_code + number + "*" + pin + "*" + monto + "#")));
            try {
                startActivity(intent);
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
        }
        else if (!PhoneNumber.isValidNumber(number)){
            //invalid number
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
        }
        else if (pin.length() != 4){
            //invalid pin
            Toast.makeText(this, R.string.invalid_pin, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()){
            finish();
            return true;
        }

        return false;
    }
}
