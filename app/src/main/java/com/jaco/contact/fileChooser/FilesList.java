package com.jaco.contact.fileChooser;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jaco.contact.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by osvel on 6/30/16.
 */
public class FilesList extends RecyclerView.Adapter<FilesList.mViewHolder> implements Serializable {

    private String currentFolder;
    private List<File> dirs;

    public FilesList(String currentFolder, File[] dirs) {

        this.currentFolder = currentFolder;

        Arrays.sort(dirs, new Comparator<File>() {
            @Override
            public int compare(File file, File file_) {
                return file.getName().toLowerCase().compareTo(file_.getName().toLowerCase());
            }
        });
        this.dirs = new ArrayList();

        //Annadir las carpetas primero
        for (File file:dirs){

            if (file.isDirectory()){
                this.dirs.add(file);
            }

        }

        //Annadir los archivos despues de las base de datos
        for (File file:dirs){

            if (file.getName().endsWith(".db")){
                this.dirs.add(file);
            }

        }

    }

    public String getCurrentFolder() {
        return currentFolder;
    }

    @Override
    public mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_file, parent, false);
        return new mViewHolder(view);
    }

    @Override
    public void onBindViewHolder(mViewHolder holder, final int position) {

        final File file = dirs.get(position);
        if (file.isDirectory())
            holder.fileIcon.setImageResource(R.drawable.folder);
        else
            holder.fileIcon.setImageResource(R.drawable.db);

        holder.fileName.setText(file.getName());

        final Activity activity = ((Activity) holder.itemView.getContext());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (file.isDirectory()){
                    currentFolder = file.getAbsolutePath();
                    RecyclerView fileChoserRecycler = (RecyclerView) activity.findViewById(R.id.file_choser);
                    try {
                        fileChoserRecycler.setAdapter(new FilesList(file.getAbsolutePath(), file.listFiles()));
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                        Toast.makeText(activity, R.string.invalid_folder, Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Intent intent = activity.getIntent();
                    intent.putExtra(FileChooserActivity.FILE_PATH, file.toString());
                    intent.putExtra(FileChooserActivity.FILE_NAME, file.getName());
                    activity.setResult(Activity.RESULT_OK, intent);
                    activity.finish();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dirs.size();
    }

    public class mViewHolder extends RecyclerView.ViewHolder {

        protected View itemView;
        protected TextView fileName;
        protected ImageView fileIcon;
        protected File current;

        public mViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            fileName = (TextView) itemView.findViewById(R.id.file_name);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
        }
    }
}
