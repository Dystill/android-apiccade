package com.dystill.app.apiccade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_WATCH_FOLDER = 1;
    private ImageView image;
    private Uri treeUri;
    private static DocumentFile directory_path = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("onCreate", "Started");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        image = (ImageView) findViewById(R.id.main_image);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.snackbar_fab, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Restore preferences
        SharedPreferences settings = getSharedPreferences("imagedata", 0);
        String treeUriString = settings.getString("imageuri", "");

        if(treeUriString.isEmpty()) {
            if (directory_path == null)
                if (image != null) image.setImageResource(R.drawable.start);
        }
        else {
            treeUri = Uri.parse(treeUriString);
            directory_path = DocumentFile.fromTreeUri(this, treeUri);
            // load images while displaying a loading circle
            new AsyncRandomImageLoad().execute();
        }
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
                // choose a folder
                sendFolderIntent();
                return true;
            case R.id.action_redo:
                // load another image
                new AsyncRandomImageLoad().execute();
                return true;
            default:
                // the user's action was not recognized.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences("imagedata", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.putString("imageuri", treeUri.toString());

        // Commit the edits!
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SELECT_WATCH_FOLDER) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                image = (ImageView) findViewById(R.id.main_image);

                // get the directory uri from the intent
                treeUri = data.getData();
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // create DocumentFile from the intent uri
                directory_path = DocumentFile.fromTreeUri(this, treeUri);

                // load images while displaying a loading circle
                new AsyncRandomImageLoad().execute();
            }
            else {
                // show a snackbar saying that no folder was selected
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, R.string.snackbar_no_folder, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_no_folder_action, new View.OnClickListener() {
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

        /* experimental: for kitkat (probably won't work)
        else {
            Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + "/myFolder/");
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setDataAndType(selectedUri, "resource/folder");
            try {
                startActivityForResult(intent, SELECT_WATCH_FOLDER);
            } catch (android.content.ActivityNotFoundException ex) {
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, "Please install a file manager.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
        /**/
    }

    // Simple Magic.
    // Taken from https://developer.android.com/guide/topics/providers/document-provider.html
    // Thank you Google.
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private class AsyncRandomImageLoad extends AsyncTask<Void, Void, Void> {

        Bitmap bitmap = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            image.setImageAlpha(0);
            // show loading circle
            findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // initialize a DocumentFile object to the Uri
            final DocumentFile file_names[] = directory_path.listFiles();

            // log all existing files inside picked directory.
            // takes up a lot of time.
            /*
            for (DocumentFile file : file_names) {
                Log.d("Listed Files", "Found file " + file.getName()
                        + " with size " + file.length());
            } /**/

            // randomly choose an image from the folder
            Random rand = new Random();
            int image_index = rand.nextInt(file_names.length);

            // take the random image uri to convert it to a bitmap image
            try {
                bitmap = getBitmapFromUri(file_names[image_index].getUri());
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {

            // if an image was found, set the main ImageView to that image
            // else show a snackbar message
            if(bitmap != null)
                image.setImageBitmap(bitmap);
            else {
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, R.string.snackbar_no_images, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            image.setImageAlpha(255);
            // hide loading circle
            findViewById(R.id.loading_panel).setVisibility(View.GONE);
        }
    }

}
