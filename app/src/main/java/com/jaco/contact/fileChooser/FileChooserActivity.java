package com.jaco.contact.fileChooser;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jaco.contact.R;

import java.io.File;

/**
 * Created by osvel on 6/30/16.
 */
public class FileChooserActivity extends AppCompatActivity {

    public static final String FILE_PATH = "FILE_PATH";
    public static final String FILE_NAME = "FILE_NAME";
    private static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_choser);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.file_chooser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String directory = null;
        if (getIntent() != null){
            directory = getIntent().getStringExtra(FILE_PATH);
        }

        File file = new File(directory == null ? SDCARD : directory);
        if (file.exists())
            directory = file.getAbsolutePath();
        else
            directory = null;

        if (directory != null && file.isFile()){
            directory = directory.replace("/"+file.getName(), "");
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.file_choser);

        if (directory == null){
            recyclerView.setAdapter(new FilesList(SDCARD, new File(SDCARD).listFiles()));
        }
        else{
            recyclerView.setAdapter(new FilesList(directory, new File(directory).listFiles()));
        }

        TextView cancel = (TextView) findViewById(R.id.cancel_action);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED, FileChooserActivity.this.getIntent());
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()){
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.file_choser);
            FilesList adapter = (FilesList) recyclerView.getAdapter();
            String path = adapter.getCurrentFolder();
            File file = new File(path);
            path = path.replace("/"+file.getName(), "");
            if (path.length() == 0)
                path = "/";
            file = new File(path);
            File[] files = file.listFiles();
            if (files != null && path != null)
                recyclerView.setAdapter(new FilesList(path, file.listFiles()));
            else return false;
        }

        return false;
    }
}
