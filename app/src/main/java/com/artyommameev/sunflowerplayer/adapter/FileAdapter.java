package com.artyommameev.sunflowerplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.artyommameev.sunflowerplayer.R;
import com.artyommameev.sunflowerplayer.domain.VideoClip;

import java.io.File;
import java.util.List;

import lombok.val;

/**
 * An adapter for presenting {@link File}s in ListView with separate icons for
 * normal files, folders and {@link VideoClip}s.
 *
 * @author Artyom Mameev
 */
public class FileAdapter extends ArrayAdapter<File> {

    private final Context context;

    /**
     * Instantiates a new File Adapter.
     *
     * @param files   the {@link File}s and {@link VideoClip}s that should be
     *                presented in ListView.
     * @param context the application context.
     */
    public FileAdapter(List<File> files, Context context) {
        super(context, R.layout.listview, files);
        this.context = context;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView,
                        @NonNull ViewGroup parent) {
        val file = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            val layoutInflater = LayoutInflater.from(getContext());

            convertView = layoutInflater.inflate(R.layout.listview, parent,
                    false);

            viewHolder.label = convertView.findViewById(R.id.label);
            viewHolder.icon = convertView.findViewById(R.id.image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (file == null) {
            throw new NullPointerException("File cannot be null");
        }

        viewHolder.label.setText(file.getName());

        int iconDrawable;

        if (file.isDirectory()) {
            iconDrawable = R.drawable.ic_folder_black_24dp;
        } else if (file instanceof VideoClip) {
            iconDrawable = R.drawable.ic_movie_black_24dp;
        } else {
            iconDrawable = R.drawable.ic_insert_drive_file_black_24dp;
        }

        viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(
                context, iconDrawable));

        return convertView;
    }

    private static class ViewHolder {
        TextView label;
        ImageView icon;
    }
}

