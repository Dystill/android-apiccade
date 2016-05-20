package com.dystill.app.apiccade;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ViewFoldersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_folders);

        Log.v("View Folders", "Started");

        RecyclerView recycler_view = (RecyclerView) findViewById(R.id.folder_recycler_view);
        FolderRecyclerAdapter adapter = new FolderRecyclerAdapter(this,
                MainActivity.getDirectoryList(), MainActivity.getImageUriLists());

        Log.v("View Folders", "Created Adapter");

        if (recycler_view != null) {
            recycler_view.setAdapter(adapter);
            recycler_view.setLayoutManager(new LinearLayoutManager(this));
            recycler_view.setItemAnimator(new SlideInRightAnimator());
        }

        adapter.notifyItemRangeChanged(0, MainActivity.getDirectoryList().size());
    }

    private class FolderRecyclerAdapter extends
            RecyclerView.Adapter<FolderRecyclerAdapter.FolderViewHolder> {

        private LayoutInflater inflater;
        private ArrayList<Uri> directories;
        private ArrayList<ArrayList<Uri>> uri_lists;

        public FolderRecyclerAdapter(Context context, ArrayList<Uri> dir, ArrayList<ArrayList<Uri>> u_list) {
            setHasStableIds(true);
            this.inflater = LayoutInflater.from(context);
            directories = dir;
            uri_lists = u_list;
        }

        @Override
        public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.v("onCreateViewHolder", "Started");
            View view = inflater.inflate(R.layout.folder_view_layout, parent, false);
            return new FolderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FolderViewHolder holder, final int position) {
            Log.v("onBindViewHolder", "Started");

            String text1 = directories.get(position).getLastPathSegment();
            String text2 = position + " - Number of images: " + uri_lists.get(position).size();

            holder.primary_text.setText(text1);
            holder.secondary_text.setText(text2);

            holder.delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(position);
                }
            });

        }

        @Override
        public long getItemId(int position) {
            return directories.get(position).hashCode();
        }

        @Override
        public int getItemCount() {
            return MainActivity.DIRECTORY_URI_LIST.size();
        }

        public class FolderViewHolder extends RecyclerView.ViewHolder {

            TextView primary_text;
            TextView secondary_text;
            Button delete_button;

            public FolderViewHolder(View itemView) {
                super(itemView);
                Log.v("ViewHolder", "Started");
                primary_text = (TextView) itemView.findViewById(R.id.folder_title);
                secondary_text = (TextView) itemView.findViewById(R.id.folder_subtitle);
                delete_button = (Button) itemView.findViewById(R.id.folder_delete_button);
            }
        }

        public void removeAt(int position) {
            MainActivity.removeDirectory(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, directories.size());
        }
    }
}
