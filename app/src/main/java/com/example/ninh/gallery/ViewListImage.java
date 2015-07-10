package com.example.ninh.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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

/**
 * Created by ninh on 04/02/2015.
 */
public class ViewListImage extends ActionBarActivity {


    String pathget;
    static GridView gridview;
    static ArrayList<Item> gridArray = new ArrayList<>();
    GridviewAdapter customGridAdapter;
    AlertDialog sortDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_list_image);

        init();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                Intent i = new Intent(getApplicationContext(),ViewFullScreen.class);
                i.putExtra("POSITION",position);
                startActivity(i);
            }
        });
    }

    private void init() {
        Bundle extras = getIntent().getExtras();
        pathget = extras.getString("PATH");

        File file = new File(pathget);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(file.getName());
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setDisplayHomeAsUpEnabled(true);

        gridview = (GridView) findViewById(R.id.gridview_viewimage);
        customGridAdapter = new GridviewAdapter(this, R.layout.row_grid, gridArray);
        gridview.setAdapter(customGridAdapter);

        GetItem get = new GetItem();
        get.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        super.onOptionsItemSelected(item);

        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.sort:

                final CharSequence[] items = {" Name "," Size "," Date "};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Sort photos by");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {

                        switch (item) {
                            case 0:

                                try {
                                    Collections.sort(gridArray, new Comparator<Item>() {
                                        @Override
                                        public int compare(Item lhs, Item rhs) {
                                            return (lhs.getTitle().compareTo(rhs.getTitle()));

                                        }
                                    });

                                    customGridAdapter.notifyDataSetChanged();
                                }
                                catch (Exception e)
                                {
                                }

                                break;

                            case 1:

                                try {
                                    Collections.sort(gridArray, new Comparator<Item>() {
                                        @Override
                                        public int compare(Item lhs, Item rhs) {

                                            File filei = new File(lhs.getPath());
                                            File filej = new File(rhs.getPath());

                                            return (int) (filei.length() - filej.length());
                                        }
                                    });

                                    customGridAdapter.notifyDataSetChanged();
                                }
                                catch (Exception e)
                                {
                                }
                                break;

                            case 2:

                                try {
                                    Collections.sort(gridArray, new Comparator<Item>() {
                                        @Override
                                        public int compare(Item lhs, Item rhs) {

                                            File filei = new File(lhs.getPath());
                                            File filej = new File(rhs.getPath());

                                            return (int) (filej.lastModified() - filei.lastModified());
                                        }
                                    });

                                    customGridAdapter.notifyDataSetChanged();
                                }
                                catch (Exception e)
                                {
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


    private class GetItem extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {



            File file = new File(pathget);

            if (file.isDirectory()) {

                final File[] listFile = file.listFiles();

                for (int i = 0; i < listFile.length; i++) {
                    if (ViewListImage.this.isFinishing()) {
                        gridArray.clear();
                        break;
                    }
                    else
                    {
                        if(!listFile[i].isDirectory()) {
                            DecodeBitmap de = new DecodeBitmap();
                            Bitmap bm = de.decodeSampledBitmapFromPath(listFile[i].getAbsolutePath(), 80, 80);

                            if (bm != null) {
                                gridArray.add(new Item(bm, listFile[i].getName(), listFile[i].getAbsolutePath()));
                                publishProgress();
                            }
                        }
                    }

                }
            }
            return "";
        }


        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {
            if (!ViewListImage.this.isFinishing())
                customGridAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        gridArray.clear();
        customGridAdapter.notifyDataSetChanged();
        finish();
        super.onBackPressed();
    }

}
