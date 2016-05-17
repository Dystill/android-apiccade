package com.dystill.app.apiccade;

import android.content.ClipData;
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
    private static final int SELECT_IMAGES_KITKAT = 2;
    private ImageView image;
    private Uri treeUri;
    private static DocumentFile directory_path = null;
    private final ArrayList<ArrayList<Uri>> image_uri_lists = new ArrayList<>();
    // private final ArrayList<Uri> image_directories = new ArrayList<>();
    private int prev_image_folder;
    private int prev_image_position;
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
                showSnackbarInMain(R.string.snackbar_fab);                                          // placeholder snackbar on click
            }
        });

        Log.v("onCreate", "Preferences");
        SharedPreferences settings = getSharedPreferences("imagedata", 0);                          // restore preferences

        amount_of_folders = settings.getInt("amount_of_folders", 0);                                // obtain the amount of folders (rows) added previously
        prev_image_folder = settings.getInt("current_folder", 0);                                   // obtain the folder index of the last obtained image
        prev_image_position  = settings.getInt("current_position", 0);                              // obtain its position in the folder

        Log.v("onCreate", "Position: " + prev_image_folder + " " + prev_image_position);

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
            image.setImageBitmap(getImageFrom2d(
                    image_uri_lists, prev_image_folder, prev_image_position));                      //      get the previously displayed image to redisplay
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
        editor.putInt("amount_of_folders", image_uri_lists.size());            // add entry for the amount of folders
        editor.putInt("current_folder", prev_image_folder);            // add entry for the amount of folders
        editor.putInt("current_position", prev_image_position);            // add entry for the amount of folders

        Log.v("onDestroy", "Start image uri writing");
        for(int f = 0; f < image_uri_lists.size(); f++) {                                           // LOOP through all rows of the 2d arraylist
            Log.v("onDestroy", "Folder " + f);
            for (int i = 0; i < image_uri_lists.get(f).size(); i++) {                                        //      LOOP through each uri of the arraylist to the end
                Log.v("onDestroy", "Adding imageuri_f" + f + "_i" + i);
                editor.putString("imageuri_f" + f + "_i" + i,
                        image_uri_lists.get(f).get(i).toString());                                           //          store each entry into the pref file
            }
            image_uri_lists.get(f).clear();                                                                  //      clear the row when the end is reached
        }
        image_uri_lists.clear();

        editor.commit();                                                                            // Commit the Edits!
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {                                                 ////// onCreateOptionsMenu() //////
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                           ////// onOptionsItemSelected() //////
        switch (item.getItemId()) {

            case R.id.action_settings:                                                              // settings action button
                return true;

            case R.id.action_add:                                                                   // add folder action button
                sendAddFolderIntent();                                                                 // call sendAddFolderIntent()
                return true;

            case R.id.action_redo:                                                                  // redo action button
                Log.v("redo", "Started");
                if(image_uri_lists.size() == 0) {                                                   // IF there are no folders
                    Log.v("redo", "no folder");
                    showSnackbarInMain(R.string.snackbar_no_folders, R.string.button_snackbar_add);                                                             //      show a snackbar saying that folder is already used
                }
                else if (image_uri_lists.size() == 1 && image_uri_lists.get(0).size() == 1 + 1) {   // ELSE IF there is only one image (+ one directory)
                    Log.v("redo", "no folder");
                    showSnackbarInMain(R.string.snackbar_no_redo, R.string.button_snackbar_add);    // show a snackbar
                }
                else if (image_uri_lists.size() > 0) {                                              // IF a folder have been added
                    Log.v("redo", "setting random image: " + image_uri_lists.size() +
                            " " + image_uri_lists.get(0).size());
                    image.setImageBitmap(getRandomImageFrom2d(image_uri_lists));                    //      re-roll an image out of all folders
                }
                else
                    showSnackbarInMain(R.string.error_generic);
                return true;

            case R.id.action_clear:                                                                 // clear cache action button
                image.setImageResource(R.drawable.start);
                for(int f = 0; f < image_uri_lists.size(); f++) {                                   // LOOP through all rows of the 2d arraylist
                    image_uri_lists.get(f).clear();
                }
                image_uri_lists.clear();
                showSnackbarInMain(R.string.snackbar_clear);
                return true;

            default:                                                                                // the user's action was not recognized.
                return super.onOptionsItemSelected(item);
        }
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

                if (!alreadyUsed(image_uri_lists, treeUri)) {                                       //      IF the folder was already selected previously -> calls alreadyUsed()
                    Log.v("onActivityResult", "Else");
                    directory_path = DocumentFile.fromTreeUri(this, treeUri);                       //          convert the uri from the intent to a DocumentFile
                    new AsyncFolderLoad().execute();                                                //          call the AsyncFolderLoad AsyncTask
                }
                else {                                                                              //      ELSE
                    showSnackbarInMain(R.string.snackbar_used_folder, R.string.button_snackbar_retry);                                                              //          show a snackbar saying that folder is already used
                }
            }
            else {
                Log.v("onActivityResult", "Else");                                                  // ELSE
                showSnackbarInMain(R.string.snackbar_no_folder, R.string.button_snackbar_retry);                                                                  //      show a snackbar saying no folder was selected
            }
        }
        else if(requestCode == SELECT_IMAGES_KITKAT) {
            if (resultCode == RESULT_OK) {                                                          // IF the request was successful
                image = (ImageView) findViewById(R.id.main_image);                                  //      find the ImageView

                if(data != null) {
                    ClipData clip = data.getClipData();
                    ArrayList<Uri> temp_uri_list = new ArrayList<>();

                    Log.v("kitkat", "clip and temp: #" + clip.getItemCount());

                    for (int i = 0; i < clip.getItemCount(); i++) {
                        ClipData.Item item = clip.getItemAt(i);
                        Log.v("kitkat", "Adding: " + item.getUri().getPath());
                        temp_uri_list.add(item.getUri());
                        Log.v("kitkat", "Added: " + item.getUri().getPath());
                    }

                    image_uri_lists.add(temp_uri_list);
                    amount_of_folders = image_uri_lists.size();
                    image.setImageBitmap(getRandomImageFrom2d(image_uri_lists));
                }
            }
            else {
                Log.v("onActivityResult", "Else");                                                  // ELSE
                showSnackbarInMain(R.string.snackbar_no_folder, R.string.button_snackbar_retry);                                                                  //      show a snackbar saying no folder was selected
            }
        }
    }

    private void sendAddFolderIntent() {                                                               ////// sendFolderIntent() //////
        Intent intent;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {                     // IF lollipop or greater
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);                                  //      send intent to DocumentsProvider to select a directory
            startActivityForResult(intent, SELECT_WATCH_FOLDER);
        }
        else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            Log.v("kitkat", "intent made");
            if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                startActivityForResult(intent, SELECT_IMAGES_KITKAT);
                Log.v("kitkat", "intent passed");
            }
            else {
                // if you reach this place, it means there is no any file
                // explorer app installed on your device
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
            Log.v("alreadyUsed", "Second");
            for (int f = 0; f < uri_list_2d.size(); f++) {                                          // LOOP through each row of the current 2d arraylist
                Log.v("alreadyUsed", "Folder #" + f);
                Log.v("alreadyUsed", tree.getPath() + " vs " + uri_list_2d.get(f).get(0).getPath());
                if (tree.equals(uri_list_2d.get(f).get(0))) {                                            //      IF the first entry of the row is the same as the selected directory
                    return true;                                                                    //          return TRUE (YES alreadyUsed)
                }
            }
        }
        Log.v("alreadyUsed", "End");                                                                // method can reach this point if the selected directory has no images
        return false;                                                                               // return FALSE in this case (NOT alreadyUsed)
                                                                                                    // this instance is handled later in the AsyncTask
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {                                   ////// getBitmapFromUri() a.k.a. Magic. //////
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");                                  // Taken from https://developer.android.com/guide/topics/providers/document-provider.html
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();                   // Thank you Google.
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }



    ////// getImage methods //////

    private Bitmap getImageFrom2d(ArrayList<ArrayList<Uri>> uri_list_2d, int f, int i) {                               ////// getRandomImage()  //////
        try {
            return getBitmapFromUri(uri_list_2d.get(f).get(i));                                               // return a bitmap of the randomly selected uri
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getRandomImage(ArrayList<Uri> uri_list, int f) {                               ////// getRandomImage()  //////

        Log.v("getRandomImage", "Started");
        Random rand = new Random();                                                                 // initialize Random object
        int i = rand.nextInt(uri_list.size() - 1) + 1;                                              // get a random, valid image index
                                                                                                    //      the "+ 1" accounts for the first item being the parent folder uri
        prev_image_folder = f;
        prev_image_position = i;

        Log.v("getRandomImage", "Trying image #" + i + "of" + uri_list.size());
        try {
            return getBitmapFromUri(uri_list.get(i));                                               // return a bitmap of the randomly selected uri
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap getRandomImageFrom2d(ArrayList<ArrayList<Uri>> uri_list_2d) {                    ////// getRandomImageFrom2d  //////

        int offset = (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 1 : 0);

        Log.v("getRandomImage2D", "Started");
        Random rand = new Random();                                                                 // initialize Random object
        int f, i;

        do {
            f = rand.nextInt(uri_list_2d.size());                                                   // get a random, valid folder index
            i = rand.nextInt(uri_list_2d.get(f).size() - offset) + offset;                                         // get a random, valid image index
            Log.v("getRandomImage2D", "Checking image #" + i + "in folder #" + f);
        } while (i == prev_image_position && f == prev_image_folder && uri_list_2d.size() > 1);
                                                                                                    //      the "+ 1" accounts for the first item being the parent folder uri

        Log.v("getRandomImage2D", "Choosing image #" + i + "in folder #" + f);
        prev_image_folder = f;
        prev_image_position = i;

        try {
            return getBitmapFromUri(uri_list_2d.get(f).get(i));                                          // return a bitmap of the selected uri
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    ////// generic Snackbar methods //////

    private void showSnackbarInMain(int message, int button) {                                      // generic snackbar with button
        View view = findViewById(R.id.scroll_view);
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendAddFolderIntent();
                    }
                }).show();
    }

    private void showSnackbarInMain(int message) {                                                  // generic snackbar without button
        View view = findViewById(R.id.scroll_view);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }




    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////// AsyncFolderLoad class //////                                                             ////// AsyncFolderLoad class //////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class AsyncFolderLoad extends AsyncTask<Void, Void, Void> {

        final DocumentFile file_names[] = directory_path.listFiles();                               // get list of files
        final ArrayList<Uri> temp_uri_list = new ArrayList<>();                                     // create a temp array
        final View loader = findViewById(R.id.loading_panel);                                       // get the loading circle panel

        @Override
        protected void onPreExecute() {                                                             ////// onPreExecute //////
            Log.v("Async", "PreStarted");
            super.onPreExecute();
            image.setImageAlpha(0);                                                                 // hide previous image (used instead of setVisibility() to preserve image dimensions)
            if(loader != null) loader.setVisibility(View.VISIBLE);                                                       // show loading circle
        }

        @Override
        protected Void doInBackground(Void... params) {                                             ////// doInBackground //////
            Log.v("Async", "doStarted");

            temp_uri_list.add(treeUri);                                                             // add the directory path to the first spot

            int i = 0;
            for (DocumentFile file : file_names) {                                                  // LOOP through all elements in the list of files
                if(file.getName().matches("(.*).(png|jpg|bmp)")) {                                  //      IF file is an image
                    temp_uri_list.add(file_names[i++].getUri());                                    //      add the image's uri to the temp array
                }
                else
                    Log.d("Listed Files", file.getName() + " Skipped.");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void params) {                                                 ////// onPostExecute //////
            Log.v("Async", "postStarted");

            if(temp_uri_list.size() > 1) {                                                          // IF the temp array has no image uris (only 1 for the directory uri added in doInBackground())
                image.setImageBitmap(getRandomImage(temp_uri_list, image_uri_lists.size()));        //      call getRandomImage()
                image_uri_lists.add(temp_uri_list);                                                 //      add the temp array to the 2d array
                amount_of_folders = image_uri_lists.size();                                         //      update the amount of folders
            }
            else {                                                                                  // ELSE
                showSnackbarInMain(R.string.snackbar_no_images);                                          //      show a snackbar saying no images were found
            }

            image.setImageAlpha(255);                                                               // make the ImageView opaque

            if(loader != null) loader.setVisibility(View.GONE);                                     // hide the loading circle
        }
    }
}
