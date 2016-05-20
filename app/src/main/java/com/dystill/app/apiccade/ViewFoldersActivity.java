package com.dystill.app.apiccade;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class ViewFoldersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_folders);
        RecyclerView recycler_view = (RecyclerView) findViewById(R.id.folder_recycler_view);
        RecyclerAdapter adapter = new RecyclerAdapter(this);

        recycler_view.setAdapter(adapter);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        recycler_view.setItemAnimator(new DefaultItemAnimator());
    }
}
