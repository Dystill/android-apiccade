package com.dystill.app.apiccade;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    LinearLayout container;

    public RecyclerAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.folder_view_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ArrayList<Uri> directories = MainActivity.getDirectoryList();
        ArrayList<ArrayList<Uri>> uri_lists = MainActivity.getImageListList();
        String secondary_string = position + " - Number of items: " + uri_lists.get(position).size();

        holder.primary_text.setText(directories.get(position).getPath());
        holder.secondary_text.setText(secondary_string);

        holder.delete_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Delete not implemented yet. Sorry!", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return MainActivity.DIRECTORY_URI_LIST.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView primary_text;
        TextView secondary_text;
        Button delete_button;

        public ViewHolder(View itemView) {
            super(itemView);
            primary_text = (TextView) itemView.findViewById(R.id.folder_title);
            secondary_text = (TextView) itemView.findViewById(R.id.folder_subtitle);
            delete_button = (Button) itemView.findViewById(R.id.folder_delete_button);
        }
    }
}