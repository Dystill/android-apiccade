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
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_WATCH_FOLDER = 1;
    private ImageView image;
    private Uri treeUri;
    private static DocumentFile directory_path = null;
    private ArrayList<ArrayList<Uri>> image_uri_lists = new ArrayList<>();
    private int amount_of_folders = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {                                            ////// onCreate() //////
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.v("onCreate", "Started");
        image = (ImageView) findViewById(R.id.main_image);                                          // get the ImageView from the starting activity

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);                   // create the fab
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.snackbar_fab, Snackbar.LENGTH_LONG)                    // placeholder snackbar on click
                        .setAction("Action", null).show();
            }
        });

        Log.v("onCreate", "Preferences");
        SharedPreferences settings = getSharedPreferences("imagedata", 0);                          // restore preferences
        if(settings.contains("amount_of_folders")) {                                                // IF the preference file has been written to yet
            Log.v("onCreate", "Preferences contained folders");
            amount_of_folders = Integer.parseInt(settings.getString("amount_of_folders", ""));      //      obtain the amount of folders (rows) added previously
        }

        Log.v("onCreate", "Getting image uris");
        int i;
        ArrayList<Uri> temp_uri_list;                                                               // create a temporary arraylist holder

        for(int f = 0; f < amount_of_folders; f++) {                                                // LOOP through image uris of each folder
            Log.v("onCreate", "Folder " + f);
            temp_uri_list = new ArrayList<>();                                                      //      allocate new pointer for next folder
            i = 0;
            while (settings.contains("imageuri_f" + f + "_i" + i)) {                                //      LOOP while there are still uris in the file
                temp_uri_list.add(Uri.parse(settings
                        .getString("imageuri_f" + f + "_i" + i++, "")));                            //          add each stored uri to the temp arraylist
                Log.v("onCreate", "Added " + "imageuri_f" + f + "_i" + i);
            }
            image_uri_lists.add(temp_uri_list);                                                     //      add the temp arraylist to the 2d arraylist
        }

        if(image_uri_lists.isEmpty()) {                                                             // IF there are no folders
            if (image != null) image.setImageResource(R.drawable.start);                            //      use a starting image in drawable
        }
        else {                                                                                      // ELSE
            image.setImageBitmap(getRandomImageFrom2d(image_uri_lists));                            //      call getRandomImage()
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           ////// onOptionsItemSelected() //////
        switch (item.getItemId()) {
            case R.id.action_settings:                                                              // settings action button
                return true;
            case R.id.action_folder:                                                                // folder action button
                sendFolderIntent();                                                                 // call sendFolderIntent()
                return true;
            case R.id.action_redo:                                                                  // redo action button
                if(image_uri_lists.size() > 1)                                                      // IF images have been added
                    image.setImageBitmap(getRandomImageFrom2d(image_uri_lists));                    //      re-roll an image out of all folders
                return true;
            default:                                                                                // the user's action was not recognized.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onDestroy() {                                                                    ////// onDestroy() //////
        super.onDestroy();
        Log.v("onDestroy", "Started");
        SharedPreferences settings = getSharedPreferences("imagedata", 0);                          // load the pref file "imagedata"
        SharedPreferences.Editor editor = settings.edit();                                          // create an editor to add data
        editor.clear();                                                                             // clear previous data

        Log.v("onDestroy", "Write folder #");
        editor.putString("amount_of_folders", Integer.toString(image_uri_lists.size()));                 // add entry for the amount of folders

        Log.v("onDestroy", "Start image uri writing");
        ArrayList<Uri> temp_uri_list;                                                               // temp arraylist
        for(int f = 0; f < image_uri_lists.size(); f++) {                                           // LOOP through all rows of the 2d arraylist
            Log.v("onDestroy", "Folder " + f);
            temp_uri_list = image_uri_lists.get(f);                                                 //      load a row into the array list
            for (int i = 0; i < temp_uri_list.size(); i++) {                                        //      LOOP through each uri of the arraylist to the end
                Log.v("onDestroy", "Adding imageuri_f" + f + "_i" + i);
                editor.putString("imageuri_f" + f + "_i" + i,
                        temp_uri_list.get(i).toString());                                           //          store each entry into the pref file
            }
            temp_uri_list.clear();                                                                  //      clear the row when the end is reached
        }
        image_uri_lists.clear();

        // Commit the edits!
        editor.commit();                                                                            // commit changes
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {                 ////// onActivityResult() //////
        Log.v("onActivityResult", "Started");
        if(requestCode == SELECT_WATCH_FOLDER) {
            if (resultCode == RESULT_OK) {                                                          // IF the request was successful
                image = (ImageView) findViewById(R.id.main_image);                                  //      find the ImageView

                treeUri = data.getData();                                                           //      get the intent data (uri of the selected folder)
                getContentResolver().takePersistableUriPermission(treeUri,                          //      permission
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Log.v("onActivityResult", "If");

                ArrayList<Uri> temp_uri_list = new ArrayList<>();                                   //      create a temp arraylist
                if (!alreadyUsed(image_uri_lists, treeUri)) {                                       //      IF the folder was already selected previously -> calls alreadyUsed()
                    Log.v("onActivityResult", "Else");
                    directory_path = DocumentFile.fromTreeUri(this, treeUri);                       //          convert the uri from the intent to a DocumentFile
                    new AsyncFolderLoad().execute();                                                //          call the AsyncFolderLoad AsyncTask
                }
                else {                                                                              //      ELSE
                    View view = findViewById(R.id.linear_view);                                     //          get main layout view
                    Snackbar.make(view, R.string.snackbar_folder_exists, Snackbar.LENGTH_LONG)      //          show a snackbar saying that folder is already used
                            .setAction(R.string.snackbar_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendFolderIntent();
                                }
                            }).show();
                }
            }
            else {
                Log.v("onActivityResult", "Else");                                                  // ELSE
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, R.string.snackbar_no_folder, Snackbar.LENGTH_LONG)              //      show a snackbar saying no folder was selected
                        .setAction(R.string.snackbar_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendFolderIntent();
                            }
                        }).show();
            }
        }
    }

    private boolean alreadyUsed(ArrayList<ArrayList<Uri>> uri_list_2d, Uri tree) {                  ////// alreadyUsed() //////
        Log.v("alreadyUsed", "Started");
        if(uri_list_2d.size() == 0) {                                                               // IF no folder was ever selected previously
            Log.v("alreadyUsed", "First");
            return false;                                                                           //      return FALSE (NOT alreadyUsed)
        }
        else {                                                                                      // ELSE (if there was a folder selected previously)
            ArrayList<Uri> uri_list_temp;                                                           //      create a temp arraylist
            Log.v("alreadyUsed", "Second");
            for (int f = 0; f < uri_list_2d.size(); f++) {                                          // LOOP through each row of the current 2d arraylist
                Log.v("alreadyUsed", "Folder #" + f);
                uri_list_temp = uri_list_2d.get(f);                                                 //      store rows into temp arraylist
                Log.v("alreadyUsed", tree.getPath() + " vs " + uri_list_temp.get(0).getPath());
                if (tree.equals(uri_list_temp.get(0))) {                                            //      IF the first entry of the row is the same as the selected directory
                    return true;                                                                    //          return TRUE (YES alreadyUsed)
                }
            }
        }
        Log.v("alreadyUsed", "End");                                                                // method can reach this point if the selected directory has no images
        return false;                                                                               // return FALSE in this case (NOT alreadyUsed)
                                                                                                    // this instance is handled later in the AsyncTask
    }

    // sends an intent to obtain a file directory
    private void sendFolderIntent() {                                                               ////// sendFolderIntent() //////
        Intent intent;
        // check for android build version to perform the proper intent
        // for devices lollipop or greater
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {                     // IF lollipop or greater
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);                                  //      send intent to DocumentsProvider to select a directory
            startActivityForResult(intent, SELECT_WATCH_FOLDER);
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {                                   ////// getBitmapFromUri() a.k.a. Magic. //////
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");                                  // Taken from https://developer.android.com/guide/topics/providers/document-provider.html
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();                   // Thank you Google.
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private Bitmap getRandomImage(ArrayList<Uri> uri_list) {                                        ////// getRandomImage()  //////

        Log.v("getRandomImage", "Started");
        Random rand = new Random();                                                                 // initialize Random object
        int image_index = rand.nextInt(uri_list.size() - 1) + 1;                                        // get a random, valid image index
                                                                                                    //      the "+ 1" accounts for the first item being the parent folder uri

        Log.v("getRandomImage", "Trying image #" + image_index + "of" + uri_list.size());
        try {
            return getBitmapFromUri(uri_list.get(image_index));                                     // return a bitmap of the randomly selected uri
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap getRandomImageFrom2d(ArrayList<ArrayList<Uri>> uri_list_2d) {                    ////// getRandomImageFrom2d  //////

        ArrayList<Uri> uri_list_temp;                                                               // create a temp array

        Log.v("getRandomImage2D", "Started");
        Random rand = new Random();                                                                 // initialize Random object
        int folder_index = rand.nextInt(uri_list_2d.size());                                        // get a random, valid folder index

        uri_list_temp = uri_list_2d.get(folder_index);                                              // store the random folder/row into the temp array
        int image_index = rand.nextInt(uri_list_temp.size() - 1) + 1;                                   // get a random, valid image index
                                                                                                    //      the "+ 1" accounts for the first item being the parent folder uri

        Log.v("getRandomImage2D", "Trying image #" + image_index + "in folder #" + folder_index);
        try {
            return getBitmapFromUri(uri_list_temp.get(image_index));                                // return a bitmap of the selected uri
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private class AsyncFolderLoad extends AsyncTask<Void, Void, Void> {                             /*/***////// AsyncFolderLoad class //////***/*/

        final DocumentFile file_names[] = directory_path.listFiles();                               // get list of files
        ArrayList<Uri> uri_list_temp = new ArrayList<>();                                           // create a temp array

        @Override
        protected void onPreExecute() {                                                             ////// onPreExecute //////
            Log.v("Async", "PreStarted");
            super.onPreExecute();
            image.setImageAlpha(0);                                                                 // hide previous image (used instead of setVisibility() to preserve image dimensions)
            findViewById(R.id.loading_panel).setVisibility(View.VISIBLE);                           // show loading circle
        }

        @Override
        protected Void doInBackground(Void... params) {                                             ////// doInBackground //////
            Log.v("Async", "doStarted");

            uri_list_temp.add(treeUri);                                                             // add the directory path to the first spot

            int i = 0;
            for (DocumentFile file : file_names) {                                                  // LOOP through all elements in the list of files
                if(file.getName().matches("(.*).(png|jpg|bmp)")) {                                  //      IF file is an image
                    uri_list_temp.add(file_names[i++].getUri());                                    //      add the image's uri to the temp array
                }
                else
                    Log.d("Listed Files", file.getName() + " Skipped.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {                                                 ////// onPostExecute //////
            Log.v("Async", "postStarted");

            if(uri_list_temp.size() > 1) {                                                          // IF the temp array has no image uris (only 1 for the directory uri added in doInBackground())
                image.setImageBitmap(getRandomImage(uri_list_temp));                                //      call getRandomImage()
                image_uri_lists.add(uri_list_temp);                                                 //      add the temp array to the 2d array
                amount_of_folders = image_uri_lists.size();                                         //      update the amount of folders
            }
            else {                                                                                  // ELSE
                View view = findViewById(R.id.linear_view);
                Snackbar.make(view, R.string.snackbar_no_images, Snackbar.LENGTH_LONG)              //      show a snackbar saying no images were found
                        .setAction("Action", null).show();
            }

            image.setImageAlpha(255);                                                               // make the ImageView opaque
            findViewById(R.id.loading_panel).setVisibility(View.GONE);                              // hide the loading circle
        }

    }

}
