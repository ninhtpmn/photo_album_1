package com.example.ninh.gallery;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity_ViewListAlbum extends ActionBarActivity {

    private int columnIndex;
    GridView gridview;
    ArrayList<Item> gridArray = new ArrayList<>();
    GridviewAdapter customGridAdapter;
    ArrayList<String> pathlist = new ArrayList<>();
    AlertDialog sortDialog;
    Cursor cursor;


            @Override
    public void onCreate(Bundle savedInstanceState) {
                int width = getWindowManager().getDefaultDisplay().getWidth();
                int height = getWindowManager().getDefaultDisplay().getHeight();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle("Albums");
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
                actionBar.setIcon(R.drawable.ic_launcher);


                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

                cursor = getContentResolver().query(uri, projection, null, null, null);
                columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                Log.i("COLUMN INDEX", ""+cursor.getCount());


                gridview = (GridView) findViewById(R.id.gridview);
                customGridAdapter = new GridviewAdapter(this, R.layout.row_grid, gridArray);

                GetItem get = new GetItem();
                get.execute();

                  gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    Intent i = new Intent(getApplicationContext(), ViewListImage.class);
                    i.putExtra("PATH", gridArray.get(position).getPath());
                    startActivity(i);
                     }
                     });

            }



     private boolean Check(String a, ArrayList<String> list) {
         if(list.isEmpty()) return false;
         else
         {
             for(int i = 0; i<list.size(); i++)
             {
                 if(a.equals(list.get(i))) return true;
             }
         }
         return  false;
     }


     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch(item.getItemId())
        {
            case R.id.sort:

                final CharSequence[] items = {" Name "," Size "," Date "};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sort albums by");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:

                                Collections.sort(gridArray,new Comparator<Item>() {
                                    @Override
                                    public int compare(Item lhs, Item rhs) {
                                      return(lhs.getTitle().compareTo(rhs.getTitle()));

                                    }
                                });

                                gridview.setAdapter(customGridAdapter);

                                break;

                            case 1:

                                Collections.sort(gridArray,new Comparator<Item>() {
                                    @Override
                                    public int compare(Item lhs, Item rhs) {

                                        File filei = new File(lhs.getPath());
                                        File filej = new File(rhs.getPath());

                                        return (int)(getfolderSize(filei) - getfolderSize(filej));
                                    }
                                });

                                        gridview.setAdapter(customGridAdapter);
                                break;
                            case 2:


                                for (int i = 0; i < gridArray.size(); i++)
                                {
                                    Collections.sort(gridArray,new Comparator<Item>() {
                                        @Override
                                        public int compare(Item lhs, Item rhs) {

                                            File filei = new File(lhs.getPath());
                                            File filej = new File(rhs.getPath());

                                            return (int)(filej.lastModified() - filei.lastModified());
                                        }
                                    });

                                        gridview.setAdapter(customGridAdapter);
                                }
                                break;
                        }
                        sortDialog.dismiss();
                    }
                });
                sortDialog = builder.create();
                sortDialog.show();

        }

        return super.onOptionsItemSelected(item);
    }

     public static long getfolderSize(File directory) {
         long length = 0;
         for (File file : directory.listFiles()) {
             if (file.isFile())
                 length += file.length();
             else
                 length += getfolderSize(file);
         }
         return length;
     }


    private class GetItem extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            if (cursor != null) {
                while (cursor.moveToNext()) {

                    int imageID = cursor.getInt(columnIndex);
                    String path = retrieve(getContentResolver(), Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imageID));
                    File file = new File(path);
                    if (file.exists())
                    {
                        if (Check(file.getParent(), pathlist) == false) {
                            pathlist.add(file.getParent());

                            DecodeBitmap de = new DecodeBitmap();
                            Bitmap bm = de.decodeSampledBitmapFromPath(file.getAbsolutePath(), 80, 80);

                            gridArray.add(new Item(bm, file.getParentFile().getName(), file.getParent()));

                            publishProgress();
                        }
                    }
                }
            }
            cursor.close();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {

            gridview.setAdapter(customGridAdapter);

        }
    }

    public static String retrieve(ContentResolver resolver, Uri uri)
    {
        if (uri.getScheme().equals("file"))
        {
            return uri.getPath();
        }
        final Cursor cursor = resolver.query(uri, new String[]{"_data"}, null, null, null);
        if (cursor.moveToFirst())
        {
            return cursor.getString(0);
        }
        throw new RuntimeException("Can't retrieve path from uri: " + uri.toString());
    }

}




