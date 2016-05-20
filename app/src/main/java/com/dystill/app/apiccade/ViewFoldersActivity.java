package com.dystill.app.apiccade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

public class ViewFoldersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_folders);

        Log.v("View Folders", "Started");

        RecyclerView recycler_view = (RecyclerView) findViewById(R.id.folder_recycler_view);
        RecyclerAdapter adapter = new RecyclerAdapter(this, MainActivity.getDirectoryList());
        adapter.notifyItemRangeChanged(0, MainActivity.getDirectoryList().size());

        Log.v("View Folders", "Created Adapter");

        if (recycler_view != null) {
            recycler_view.setAdapter(adapter);
            recycler_view.setLayoutManager(new LinearLayoutManager(this));
            recycler_view.setItemAnimator(new DefaultItemAnimator());
        }
    }
}
