package com.jaco.contact.fileChooser;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
    private OnFileClickListener listener;
    private List<File> dirs;

    public FilesList(String currentFolder, File[] dirs, OnFileClickListener listener) {

        this.listener = listener;
        this.currentFolder = currentFolder;

        if (dirs == null)
            dirs = new File[0];

        Arrays.sort(dirs, new Comparator<File>() {
            @Override
            public int compare(File file, File file_) {
                return file.getName().toLowerCase().compareTo(file_.getName().toLowerCase());
            }
        });
        this.dirs = new ArrayList();

        //Annadir las carpetas primero
        for (File file:dirs)
            if (file.isDirectory())
                this.dirs.add(file);

        //Annadir los archivos despues de las base de datos
        for (File file:dirs)
            if (file.getName().endsWith(".db"))
                this.dirs.add(file);

    }

    public void setOnFileClickListener(OnFileClickListener listener) {
        this.listener = listener;
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onFileClicked(file);
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

    public interface OnFileClickListener{
        void onFileClicked(File file);
    }
}
