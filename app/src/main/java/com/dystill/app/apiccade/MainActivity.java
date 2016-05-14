package com.dystill.app.apiccade;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_WATCH_FOLDER = 1;
    private static String image_directory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView image = (ImageView) findViewById(R.id.main_image);
        if (image != null) {
            image.setImageResource(R.drawable.start);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // go to settings
                return true;
            case R.id.action_folder:
                // if choosing a watch folder
                sendFolderIntent();
                return true;
            default:
                // the user's action was not recognized.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_WATCH_FOLDER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // translate intent data
                Uri uri = data.getData();
                // set the image directory to the string taken from the uri
                image_directory = uri.toString();
            }
            else {
                // show a snackbar saying tha no folder was selected
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, "No folder selected.", Snackbar.LENGTH_LONG)
                        .setAction("Try Again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendFolderIntent();
                            }
                        }).show();
            }

        }
    }

    // sends an intent to obtain a file directory
    private void sendFolderIntent() {
        Intent intent;

        // check for android build version to perform the proper intent
        // for devices lollipop or greater
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, SELECT_WATCH_FOLDER);
        }
        // for kitkat (probably doesn't work)
        else {
            Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/myFolder/");
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(selectedUri, "resource/folder");
            try {
                startActivityForResult(intent, SELECT_WATCH_FOLDER);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, "Please install a file manager.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }
}
